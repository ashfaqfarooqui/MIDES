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

package modelbuilding.core.interfaces.algorithms

import modelbuilding.algorithms.EquivalenceOracle.CEGenerator
import modelbuilding.algorithms.LStar.ObservationTable
import modelbuilding.core.{Alphabet, Automaton, Grammar}

trait Teacher {

  def isMember(specName: Option[String])(g: Grammar): Int

  def isHypothesisTrue(
      t: ObservationTable,
      ceGenerator: CEGenerator
    ): Either[Grammar, Boolean] = {
    val P  = (t.S ++ t.sa).filterNot(p => t.getRowValues(p).get.forall(_ == 0))
    val W  = t.E
    val h  = t.getAutomata
    val A  = h.alphabet
    val ce = ceGenerator.findCE(P, W, A, h, t.isMember)
    ce
  }

  def isHypothesisTrue(
      accessorString: Set[Grammar],
      distinguishingStrings: Set[Grammar],
      alphabet: Alphabet,
      hypAutomaton: Automaton,
      memberQuery: Grammar => Int,
      ceGenerator: CEGenerator
    ): Either[Grammar, Boolean] = {
    val ce = ceGenerator.findCE(
      accessorString,
      distinguishingStrings,
      alphabet,
      hypAutomaton,
      memberQuery
    )
    ce
  }

}
