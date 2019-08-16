package modelbuilding.algorithms.LStar

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import com.github.martincooper.datatable.{DataColumn, DataRow, DataTable}
import com.github.tototoshi.csv.CSVWriter
import com.sun.tools.jdeprscan.CSV
import grizzled.slf4j.Logging
import modelbuilding.core.{Alphabet, Automaton, Grammar, Symbol}
import modelbuilding.algorithms.LStar._
import modelbuilding.core.modelInterfaces.Teacher

import scala.util.{Failure, Success}

object ObservationTable {

  val initTable = DataTable("table",Seq(new DataColumn[String]("T",Set.empty))) match {
    case Success(value) => value
    case Failure(e) => throw new Exception(s"Couldn't create an inital table: $e")
  }

  def apply(table: DataTable, A: Alphabet, S: Set[Grammar], E: Set[Grammar],teacher: Teacher, instance: Int): ObservationTable = new ObservationTable(table, A, S, E,teacher, instance)

  def apply(A: Alphabet, S: Set[Grammar], E: Set[Grammar],teacher: Teacher, instance: Int): ObservationTable = new ObservationTable(initTable, A, S, E,teacher, instance)
  def apply(filePath:String): ObservationTable = loadtable(filePath)//new ObservationTable(table, A, S, E, instance)


  def loadtable(filePath:String):ObservationTable={
    val ois = new ObjectInputStream(new FileInputStream(filePath))
    val table = ois.readObject.asInstanceOf[ObservationTable]
    ois.close
    table

  }

  def saveTable(table: ObservationTable, filePath: String)={
    import java.io.File

    val tableFile = new File(filePath+"table.csv")
    val metadata = new File(filePath+"metaData.csv")

   // val metaWriter = CSVWriter.open(metadata)
    val oos = new ObjectOutputStream(new FileOutputStream(filePath))
    oos.writeObject(table)
    oos.close()
    //val tableWriter = CSVWriter.open(tableFile)
    //table.table.rows.foreach(r=>tableWriter.writeRow(r.values))
    //tableWriter.close()
  }


  def updateTable(oldTable:ObservationTable, newS:Set[Grammar], newE:Set[Grammar]):ObservationTable= {

    def sa = for (s <- newS; a <- oldTable.A.events) yield {
      (s + a)
    }

    val column1 = new DataColumn[String]("T", (newS union sa).map(_.toString))
    val columns = column1 :: newE.map {
      e =>
        new DataColumn[Int](s"$e", (newS union sa).map(s => oldTable.getElement(s, e).getOrElse(oldTable.teacher.isMember(s + e))))

    }.toList

    val table = DataTable(oldTable.table.name, columns) match {
      case Failure(exception) => throw new Exception(s"Requlted in an unexpected table $exception")
      case Success(value) => value
    }

    ObservationTable(table, oldTable.A, newS, newE,oldTable.teacher, oldTable.instance + 1)
  }
}

case class ObservationTable(table :DataTable, A: Alphabet, S: Set[Grammar], E: Set[Grammar], teacher:Teacher, instance:Int) extends Serializable with Logging{

  //def getInconsistent: Grammar = ???
  //def getUnclosed: Grammar = ???


  def sa = for (s <- S; a <- A.a) yield {
    (s + a)
  }

  val isClosed:Option[Set[Grammar]] = findUnclosed

  def isConsistent: Option[(Grammar,Grammar,Symbol)] ={
    //updating
    lazy val cons= for {
      s1 <- S
      s2 <- S
      a <- A.a
      if getRow(s1) == getRow(s2)  && getRow(s1+a) != getRow(s2+a)
    } yield (s1,s2,a)

   // if(cons.isEmpty) (None,true) else (Some(cons.head),false)
     cons.headOption

  }

  def findUnclosed: Option[Set[Grammar]] = {
    def iter(lst: List[Grammar], set: Set[Grammar]= Set()): Option[Set[Grammar]] =
      lst match {

        case hd::tl => {
          if( (for(s <- S  ) yield getRow(s) != getRow(hd)).forall(_==true))
            iter(tl,set+hd)
          else
            iter(tl,set)
        }
        case Nil => if(set.nonEmpty) Some(set) else None
      }
    iter(sa.toList)
  }

  def getDistinguishingSuffix(s1: Grammar,s2:Grammar,a:Symbol):Option[Grammar] ={
    E.foreach(e =>if (getElement(s1 + a, e) != getElement(s2 + a, e)){
      return Some(a+e)
    })

    None
  }
  def getRow(g:Grammar):Option[DataRow] = getRow(g.toString)
  def getRow(g:String):Option[DataRow] = table.find(_("T")==g) //filter(row=> row.as[Grammar]("T")==g).toDataTable
  def getElement(rowS:Grammar,colE:Grammar)= getRow(rowS.toString) match {
    case Some(value) => Some(value.valueMap(colE.toString).asInstanceOf[Int])
    case _ => None
  }



  def getAutomata:Automaton = ???
}
