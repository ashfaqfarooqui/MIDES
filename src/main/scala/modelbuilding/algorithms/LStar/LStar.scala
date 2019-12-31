/*
 * Learning Automata for Supervisory Synthesis
 *  Copyright (C) 2019
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package modelbuilding.algorithms.LStar

import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.CEGenerator
import modelbuilding.algorithms.LStar.ObservationTable._
import modelbuilding.core.interfaces.algorithms.Teacher
import modelbuilding.core.{Alphabet, Automata, Automaton, Grammar, Symbol, Word, tau}
import supremicastuff.SupremicaHelpers

import scala.annotation.tailrec

class LStar(
    teacher: Teacher,
    spec: Option[String],
    A: Alphabet,
    ceGen: CEGenerator)
    extends Logging {
  val t = Symbol(tau)

  def obsTable(S: Set[Grammar], E: Set[Grammar]) =
    ObservationTable(A, S, E, teacher.isMember(spec), 0)
  @tailrec
  private def learn(oTable: ObservationTable): Automaton = {
    info(s"S: ${oTable.S.size}, E: ${oTable.E.size}")
    info(s"Instance: ${oTable.instance}")
    debug(oTable.prettyPrintTable)

    if (oTable.isClosed.nonEmpty) {
      //info(s"Table is not closed ${oTable.isClosed}...closing")
      info(s"Table is not closed ...closing")
      learn(updateTable(oTable, oTable.S + oTable.isClosed.get.head, oTable.E))
    } else {
      info("checking consistent")
      val inCons = oTable.isConsistent
      if (inCons.nonEmpty) {
        info(s"Table is inconsistent")
        debug(
          s"Table is not consistent 1:${inCons.get._1} 2:${inCons.get._2} 3:${inCons.get._3}"
        )
        debug("updating table with distinguishing string")
//        info(oTable.getAutomata.toString)
        learn(
          updateTable(
            oTable,
            oTable.S,
            oTable.E + oTable
              .getDistinguishingSuffix(inCons.get._1, inCons.get._2, inCons.get._3)
              .get
          )
        )
      } else {
        info(oTable.getAutomata.toString)
        oTable.getAutomata.createDotFile
        SupremicaHelpers.exportAsSupremicaAutomata(
          Automata(Set(oTable.getAutomata.removeTauAndDump)),
          name = "hypothesis"
        )
        val counterExample = teacher.isHypothesisTrue(oTable, ceGen)
        info(s"got CE: $counterExample")
        counterExample match {
          case Right(bool) => oTable.getAutomata
          case Left(command) =>
            val toAppend: Grammar = command match {
              case w: Word   => w
              case s: Symbol => s
            }
            learn(updateTable(oTable, oTable.S ++ toAppend.getAllPrefixes, oTable.E))
        }
      }

    }
  }

  def startLearning() = {
    info("Starting Lstar Learner")
    val l = learn(updateTable(obsTable(Set(t), Set(t)), Set(t), Set(t)))
    info("Done Lstar Learner")
    l.copy(name = s"sup_${if (spec.isDefined) spec.get else "hypothesis"}")
  }
}
