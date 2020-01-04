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

package modelbuilding.algorithms.EquivalenceOracle

import grizzled.slf4j.Logging
import modelbuilding.core.{Alphabet, Automaton, Grammar, State, Symbol}

object Wmethod {
  def apply(alphabets: Alphabet, nbrState: Int): Wmethod =
    new Wmethod(alphabets, nbrState)
}

/**
 * Implementation of the WMethod from testing literature "Testing Software Design Modeled by Finite-State Machines", Chow, T, IEE Trans on Software Engineering 1978.
 * We terminate if no CE is found for 2 consecutive iterations.
 *
 * @param alphabets the event set of the system.
 * @param nbrState an estimate of the maximum states in the target system.
 */
class Wmethod(alphabets: Alphabet, nbrState: Int) extends CEGenerator with Logging {
  var CachecPwrA: Map[Int, Set[Grammar]] = Map(0 -> Set.empty[Grammar])

  private def evalString(s: Grammar, a: Automaton): Int = {
    def loop(currState: State, stringToTraverse: List[Symbol]): State = {

      stringToTraverse match {
        case x :: xs => loop(a.transitionFunction((currState, x)), xs)
        case Nil     => currState
      }
    }

    val reachedState = loop(a.getInitialState, s.getSequenceAsList)
    //debug(s"reachedstate: $reachedState")

    if (a.getMarkedState.nonEmpty && a.getMarkedState.get.contains(reachedState)) {
      2
    } else {
      if (reachedState.s != "dump:") {
        1
      } else 0
    }
  }

  override def findCE(
      accessorString: Set[Grammar],
      distinguishingStrings: Set[Grammar],
      alphabet: Alphabet,
      hypAutomaton: Automaton,
      memberQuery: Grammar => Int
    ): Either[Grammar, Boolean] = {
    //override def findCE(t: ObservationTable): Either[Grammar, Boolean] = {

    val P = accessorString        //(t.S ++ t.sa).filterNot(p=>t.getRowValues(p).get.forall(_==0))
    val W = distinguishingStrings //t.E
    val h = hypAutomaton          //t.getAutomata
    val A = alphabet.events       //h.alphabet.events

    CachecPwrA = CachecPwrA + (1 -> A.asInstanceOf[Set[Grammar]])

    def loop(n: Int, oldU: Set[Grammar]): Either[Grammar, Boolean] = {
      val i           = nbrState - h.states.size - n
      val cachedReply = CachecPwrA.get(i)
      lazy val U = if (cachedReply.isDefined) {
        cachedReply.get
      } else {
        CachecPwrA = CachecPwrA + (i -> A.flatMap(
          e => CachecPwrA(i - 1).map(a => e + a)
        ))
        CachecPwrA(i)
      }
      info(s"running for i: $i")
      if (n <= 0 || i >= 3) {
        return Right(true)
      }

      for {
        p <- P
        w <- W
        u <- U
      } {
        val s     = p + u + w
        val sysOp = memberQuery(s) //t.isMember(s)
        val hypOp = evalString(s, h)
        debug(
          s"checking for ce with $p + $u + $w + ,got sys: $sysOp, and hypOp : $hypOp"
        )
        if (sysOp != hypOp) {

          return Left(s)
        }
      }
      loop(n - 1, U)
    }

    loop(nbrState - h.states.size, A.asInstanceOf[Set[Grammar]])
  }
}
