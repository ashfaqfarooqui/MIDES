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

package modelbuilding.algorithms.KV

import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.CEGenerator
import modelbuilding.core.modelInterfaces.Teacher
import modelbuilding.core.{
  Alphabet,
  Automaton,
  Grammar,
  State,
  Symbol,
  Transition,
  Word,
  tau
}

import scala.annotation.tailrec

class LearnDT(
    teacher: Teacher,
    spec: Option[String],
    A: Alphabet,
    ceGen: CEGenerator)
    extends Logging {

  val t                        = Symbol(tau)
  val isMember: Grammar => Int = teacher.isMember(spec)

  def sift(s: Grammar, t: DiscriminationTree[Grammar]): Grammar = {
    @tailrec
    def siftHelper(currentTree: DiscriminationTree[Grammar]): Grammar =
      currentTree match {
        case Leaf(v) => v
        case Node(v, l, r) =>
          isMember(s + v) match {
            case 0 => siftHelper(l)
            case 1 => siftHelper(r)
            case 2 => siftHelper(r)

          }
        case _ =>
          throw new Exception("Encountered Tree which is not Node or leaf")
      }

    siftHelper(t)
  }

  def makeHypothesis(dTree: DiscriminationTree[Grammar]): Automaton = {
    lazy val states = dTree.getLeafValues
    lazy val accessStringsToStates =
      states.zip(Stream from 0).toMap.map {
        case (g, i) => g -> State(s"v$i")
      }

    implicit def convertToStates(g: Grammar): State = {
      accessStringsToStates(g)
    }

    lazy val transitions = for {
      s <- states
      e <- A.events
    } yield {
      Transition(s, sift(s + e, dTree), e)
    }

    Automaton(
      "FromDT",
      accessStringsToStates.values.toSet,
      A,
      transitions,
      sift(t, dTree)
    )
  }

  def updateTree(
      ce: Grammar,
      MHat: Automaton,
      DTree: DiscriminationTree[Grammar]
    ): DiscriminationTree[Grammar] = {
    def getStateReachedinMHat(str: Grammar): State = {

      def nextState(s: State, e: Symbol): State = {
        MHat.transitionFunction(s, e)
      }

      str.getSequenceAsList.foldLeft(MHat.iState)(nextState)
    }

    lazy val prefixes = ce.getAllPrefixes
    lazy val GammaJ: Grammar =
      prefixes.find(p => sift(p, DTree).toString != getStateReachedinMHat(p).toString).get

    val gammaJ1 = GammaJ.getSequenceAsList.take(GammaJ.length - 1)

    val accessStringToReplace = sift(Word(gammaJ1.tail, gammaJ1.head), DTree)

    //  DTree.update(accessStringToReplace, GammaJ)

  }

  def learnAutomaton: Automaton = {
    //Initialization
    //TODO: is member should take spec
    val iniState = State("q1")
    val transitions = A.events.map { a =>
      Transition(iniState, iniState, a)
    }
    val hyp = Automaton("initial", Set(iniState), A, transitions, iniState, None, None)
    val ce  = ceGen.findCE(Set(t), Set(t), A, hyp, isMember)

    val iniTree = ce match {
      case Left(value)  => Node[Grammar](t, Leaf(t), Leaf(value))
      case Right(value) => Node[Grammar](t, Leaf(t), Leaf(t))
    }
    //val iniTree = Node[Grammar](t, Leaf(t), Leaf(ce.left))

    //Main Loop
    @tailrec
    def mainLoop(hypTree: DiscriminationTree[Grammar]): Automaton = {
      val newHyp = makeHypothesis(hypTree)
      val newCe =
        ceGen.findCE(hypTree.getLeafValues, hypTree.getNodeValues, A, newHyp, isMember)
      newCe match {
        case Left(ce) => mainLoop(updateTree(ce, hypTree))
        case Right(b) => newHyp
      }
    }

    mainLoop(iniTree)

  }
}
