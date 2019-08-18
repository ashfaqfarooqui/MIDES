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
   private def learn(oTable: ObservationTable):Automaton = {
    info(s"S: ${oTable.S.size}, E: ${oTable.E.size}")
    info(s"Instance: ${oTable.instance}")
   debug(oTable.prettyPrintTable)


    if (oTable.isClosed.nonEmpty) {
      info(s"Table is not closed ${oTable.isClosed}...closing")
      learn(updateTable(oTable,oTable.S+oTable.isClosed.get.head,oTable.E))
    }
    else {
      info("checking consistent")
    val inCons = oTable.isConsistent
    if (inCons.nonEmpty) {
      info(s"Table is inconsistent")
      debug(s"Table is not consistent 1:${inCons.get._1} 2:${inCons.get._2} 3:${inCons.get._3}")
      debug("updating table with distinguishing string")
      learn(updateTable(oTable,oTable.S,oTable.E + oTable.getDistinguishingSuffix( inCons.get._1,inCons.get._2,inCons.get._3).get))
    }

    else
    {
      val counterExample = teacher.isHypothesisTrue(oTable, ceGen)
      info(s"got CE: $counterExample")
      counterExample match {
        case Right(bool) => oTable.getAutomata
        case Left(command) =>
          val toAppend: Grammar = command match {
            case w: Word => w
            case s: Symbol => s
          }
          learn(updateTable(oTable,oTable.S ++ toAppend.getAllPrefixes, oTable.E))
      }
    }

    }
  }



  def startLearning()= {
    info("Starting Lstar Learner")
    val l = learn(updateTable(obsTable(Set(t),Set(t)),Set(t),Set(t)))
    info("Done Lstar Learner")
    l
  }
}
