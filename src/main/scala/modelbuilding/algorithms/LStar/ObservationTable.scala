package modelbuilding.algorithms.LStar

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import com.github.martincooper.datatable.{DataColumn, DataRow, DataTable}
import grizzled.slf4j.Logging
import modelbuilding.algorithms.LStar.ObservationTable._
import modelbuilding.core.{Alphabet, Automaton, Grammar, State, Symbol, Transition, tau}

import scala.util.{Failure, Success}

object ObservationTable {

  val defaultCell00 = "T"
  val col1          = new DataColumn[String](defaultCell00, Vector.empty[String])
  val initTable = DataTable("table", Vector(col1)) match {
    case Success(value) => value
    case Failure(e)     => throw new Exception(s"Couldn't create an inital table: $e")
  }

  def apply(
      table: DataTable,
      A: Alphabet,
      S: Set[Grammar],
      E: Set[Grammar],
      isMember: Grammar => Int,
      instance: Int
    ): ObservationTable = new ObservationTable(table, A, S, E, isMember, instance)
  def apply(
      A: Alphabet,
      S: Set[Grammar],
      E: Set[Grammar],
      isMember: Grammar => Int,
      instance: Int
    ): ObservationTable = new ObservationTable(initTable, A, S, E, isMember, instance)
  def apply(filePath: String): ObservationTable =
    loadtable(filePath) //new ObservationTable(table, A, S, E, instance)

  def loadtable(filePath: String): ObservationTable = {
    val ois   = new ObjectInputStream(new FileInputStream(filePath))
    val table = ois.readObject.asInstanceOf[ObservationTable]
    ois.close
    table

  }

  /**
   * THis, save and load, functionality does not work currently.
   * 
   */
  def saveTable(table: ObservationTable, filePath: String) = {
    import java.io.File

    val tableFile = new File(filePath + "table.csv")
    val metadata  = new File(filePath + "metaData.csv")

    // val metaWriter = CSVWriter.open(metadata)
    val oos = new ObjectOutputStream(new FileOutputStream(filePath))
    oos.writeObject(table)
    oos.close()
    //val tableWriter = CSVWriter.open(tableFile)
    //table.table.rows.foreach(r=>tableWriter.writeRow(r.values))
    //tableWriter.close()
  }

  def updateTable(
      oldTable: ObservationTable,
      newS: Set[Grammar],
      newE: Set[Grammar]
    ): ObservationTable = {

    def sa =
      for {
        s <- newS
        a <- oldTable.A.events
      } yield {
        (s + a)
      }

    val column1 =
      new DataColumn[String](defaultCell00, (newS union sa).toVector.map(_.toString))
    val columns = column1 :: newE.map { e =>
      val colValues = (newS union sa).toVector.map(
        s =>
          oldTable.getElement(s, e).getOrElse {

            val se = s + e
            oldTable.isMember(se)
          }
      )
      val c = new DataColumn[Int](s"$e", colValues)
      c
    }.toList

    val table = DataTable(oldTable.table.name, columns) match {
      case Failure(exception) =>
        throw new Exception(s"Resulted in an unexpected table $exception")
      case Success(value) => value
    }

    ObservationTable(
      table,
      oldTable.A,
      newS,
      newE,
      oldTable.isMember,
      oldTable.instance + 1
    )
  }
}

case class ObservationTable(
    table: DataTable,
    A: Alphabet,
    S: Set[Grammar],
    E: Set[Grammar],
    isMember: Grammar => Int,
    instance: Int)
    extends Serializable
    with Logging {
  def prettyPrintTable: String = {
    val buf    = new StringBuilder
    val maxSa  = S.toList.map(_.toString.mkString(",").length).max
    val sufStr = E.toList.map(_.toString).mkString(" | ")
    val div    = "|" + "-" * (1 + maxSa + 1 + sufStr.length) + "|\n"
    buf ++= " \n|T" + " " * (maxSa - 1) + " | "

    buf ++= sufStr
    buf += '\n'

    buf ++= div
    for (s <- S.toList.sortWith(
           _.toString.mkString(",").length < _.toString.mkString(",").length
         )) {
      buf ++= s"| $s" + " " * (maxSa - s.toString.length) + " | "
      buf ++= getRowValues(s).get.mkString(" | ")
      buf += '\n'
    }

    buf ++= div
    for (s <- (sa diff S).toList
           .sortWith(_.toString.mkString(",").length < _.toString.mkString(",").length)) {
      buf ++= s"|$s" + " " * (maxSa - s.toString.length) + " | "
      buf ++= getRowValues(s).get.mkString(" | ")
      buf += '\n'
    }

    buf.toString

  }

  def sa =
    for {
      s <- S
      a <- A.events
    } yield {
      (s + a)
    }

  def isClosed: Option[Set[Grammar]] = findUnclosed

  def isConsistent: Option[(Grammar, Grammar, Symbol)] = {
    //updating
    lazy val cons = for {
      s1 <- S
      s2 <- S
      a  <- A.events
      if compareRows(getRowValues(s1).get, getRowValues(s2).get, _ == _) && compareRows(
        getRowValues(s1 + a).get,
        getRowValues(s2 + a).get,
        _ != _
      )
    } yield (s1, s2, a)

    // if(cons.isEmpty) (None,true) else (Some(cons.head),false)
    cons.headOption

  }

  def compareRows(row1: List[Int], row2: List[Int], e: (Any, Any) => Boolean): Boolean = {
    assert(row1.size == row2.size, "rows have different sizes")
//TODO: Use the getRowValues Instead

    e(row1, row2)
    //assert((r1.keySet diff r2.keySet).isEmpty && (r2.keySet diff r1.keySet).isEmpty, "rows have different keys" )
    //r1.forall{case(k,v)=> e(r2(k),v)}
  }
  def findUnclosed: Option[Set[Grammar]] = {
    def iter(lst: List[Grammar], set: Set[Grammar] = Set()): Option[Set[Grammar]] =
      lst match {

        case hd :: tl => {
          if ((for (s <- S)
                yield compareRows(getRowValues(s).get, getRowValues(hd).get, _ != _))
                .forall(_ == true))
            iter(tl, set + hd)
          else
            iter(tl, set)
        }
        case Nil => if (set.nonEmpty) Some(set) else None
      }
    iter(sa.toList)
  }

  def getDistinguishingSuffix(s1: Grammar, s2: Grammar, a: Symbol): Option[Grammar] = {
    E.foreach(
      e =>
        if (getElement(s1 + a, e) != getElement(s2 + a, e)) {
          return Some(a + e)
        }
    )

    None
  }
  def getRowValues(g: Grammar): Option[List[Int]] = {
    getRow(g) match {
      case Some(value) =>
        Some(
          table.columns
            .filter(_.name != defaultCell00)
            .map(
              t =>
                value.getAs[Int](t.name) match {
                  case Failure(exception) =>
                    throw new Exception(s"Row value not found $exception")
                  case Success(value) => value
                }
            )
            .toList
        )
      case None => None
    }

  }
  def getRow(g: Grammar): Option[DataRow] = getRow(g.toString)
  def getRow(g: String): Option[DataRow] =
    table.find(_(defaultCell00) == g) //filter(row=> row.as[Grammar]("T")==g).toDataTable
  def getElement(rowS: Grammar, colE: Grammar): Option[Int] = getRow(rowS) match {
    case Some(value) =>
      value.valueMap.get(colE.toString) match {
        case Some(value) => Some(value.asInstanceOf[Int])
        case None        => None
      }
    case None => None
  }

  def getAutomata: Automaton = {
    def toString(b: List[Int]): String = b.mkString("")

    lazy val getStates: Map[List[Int], State] = {
      S.map(s => getRowValues(s).get)
        .zip(Stream from 1)
        .map {
          case (i, v) =>
            if (i.forall(_ == 0)) i -> State(s"dump:") else i -> State(s"s$v")
        }
        .toMap
    }

    //F here is final, not forbidden
    def getfStates = {
      def iter(s: List[Grammar], set: Set[State] = Set()): Set[State] = {
        s match {
          case h :: tl => {
            if (getElement(h, Symbol(tau)).get == 2)
              iter(tl, set + getStates(getRowValues(h).get))
            else iter(tl, set)
          }
          case Nil => set
        }
      }

      iter(S.toList)
    }

    def getState(v: List[Int]) = getStates(v)

    def initState: State = getStates(getRowValues(Symbol(tau)).get)

    def fState: Set[State] = getfStates //Set(getStates(row(Symbol(tou)).map(_._1)))

    def forbiddenStates: Set[State] = Set.empty

    def transitions = {
      val sa = for {
        s <- S
        a <- A.events
      } yield (s, a)
      sa.map {
        case (s, a) =>
          Transition(getState(getRowValues(s).get), getState(getRowValues(s + a).get), a)
      }
    }

    lazy val states = getStates.values.toSet

    Automaton(
      "Hypothesis",
      states,
      A,
      transitions,
      initState,
      Some(fState),
      Some(forbiddenStates)
    )
  }
}
