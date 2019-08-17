package modelbuilding.algorithms.LStar

import com.github.martincooper.datatable.{DataColumn, DataTable}
import modelbuilding.algorithms.LStar
import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.CEGenerator
import modelbuilding.algorithms.LStar.ObservationTable._
import modelbuilding.core.modelInterfaces.Teacher
import modelbuilding.core.{Alphabet, Automaton, Grammar, Symbol, Word, tau}

import scala.annotation.tailrec
import scala.util.{Failure, Success}

class LStar(teacher: Teacher, A:Alphabet,ceGen:CEGenerator) extends Logging
{
  val t = Symbol(tau)

  def obsTable(S:Set[Grammar], E:Set[Grammar]) = ObservationTable(A,S,E,teacher,0)


  @tailrec
   private def learn(table: ObservationTable):Automaton = {
   // debug(table.prettyPrintTable)
    info(s"S: ${table.S.size}, E: ${table.E.size}")
    info(s"Instance: ${table.instance}")
   println(table.prettyPrintTable)


    if (table.isClosed.nonEmpty) {
      info(s"Table is not closed ${table.isClosed}...closing")
      learn(updateTable(table,table.S+table.isClosed.get.head,table.E))
    }
    else {
      info("checking consistent")
    val inCons = table.isConsistent
    if (inCons.nonEmpty) {
      ////if(!table.isConsistentOld._2){
      debug(s"Table is not consistent 1:${inCons.get._1} 2:${inCons.get._2} 3:${inCons.get._3}")
     debug("updating table with distinguishing string")
      learn(updateTable(table,table.S,table.E + table.getDistinguishingSuffix( inCons.get._1,inCons.get._2,inCons.get._3).get))
    }

    else
    {
      val counterExample = teacher.isHypothesisTrue(table, ceGen)
      info(s"got CE: $counterExample")
      counterExample match {
        case Right(bool) => table.getAutomata
        case Left(command) =>
          val toAppend: Grammar = command match {
            case w: Word => w
            case s: Symbol => s
          }
          learn(updateTable(table,table.S ++ toAppend.getAllPrefixes, table.E))
      }
    }

    }
    //else {throw new Exception("Something is wrong")}
  }



  def startLearning()= {
    info("Starting Learner")
    val l = learn(updateTable(obsTable(Set(t),Set(t)),Set(t),Set(t)))
    info("Done Learner")
    l
  }
}
