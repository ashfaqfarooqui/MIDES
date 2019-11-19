///*
// *  Learning Automata for Supervisory Synthesis
// *  Copyright (C) 2019
// *
// *     This program is free software: you can redistribute it and/or modify
// *     it under the terms of the GNU General Public License as published by
// *     the Free Software Foundation, either version 3 of the License, or
// *     (at your option) any later version.
// *
// *     This program is distributed in the hope that it will be useful,
// *     but WITHOUT ANY WARRANTY; without even the implied warranty of
// *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *     GNU General Public License for more details.
// *
// *     You should have received a copy of the GNU General Public License
// *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//
//package modelbuilding.algorithms.KV
//
//import grizzled.slf4j.Logging
//import modelbuilding.algorithms.EquivalenceOracle.CEGenerator
//import modelbuilding.core.modelInterfaces.Teacher
//import modelbuilding.core.{
//  Alphabet,
//  Automaton,
//  Grammar,
//  State,
//  Symbol,
//  Transition,
//  Word,
//  tau
//}
//
//import scala.annotation.tailrec
//
//class LearnDT(
//    teacher: Teacher,
//    spec: Option[String],
//    A: Alphabet,
//    ceGen: CEGenerator)
//    extends Logging {
//
//  val t                        = Symbol(tau)
//  val isMember: Grammar => Int = teacher.isMember(spec)
//
//  def sift(s: Grammar, t: Tree): Grammar = {
//    @tailrec
//    def siftHelper(currentTree: Tree): Grammar =
//      currentTree match {
//        case Leaf(v) => v
//        case Node(v, l, r) =>
//          isMember(s + v) match {
//            case 0 => siftHelper(l)
//            case 1 => siftHelper(r)
//            case 2 => siftHelper(r)
//
//          }
//        case _ =>
//          throw new Exception("Encountered Tree which is not Node or leaf")
//      }
//
//    siftHelper(t)
//  }
//
//  def makeHypothesis(dTree: Tree): Automaton = {
//    lazy val states = dTree.getLeafValues
//    lazy val accessStringsToStates =
//      states.zip(Stream from 0).toMap.map {
//        case (g, i) => g -> State(g.toString)
//      }
//
//    implicit def convertToStates(g: Grammar): State = {
//      accessStringsToStates(g)
//    }
//
//    lazy val transitions = for {
//      s <- states
//      e <- A.events
//    } yield {
//      Transition(s, sift(s + e, dTree), e)
//    }
//
//    Automaton(
//      "FromDT",
//      accessStringsToStates.values.toSet,
//      A,
//      transitions,
//      sift(t, dTree)
//    )
//  }
//
//  def updateTree(ce: Grammar, MHat: Automaton, DTree: Tree): Tree = {
//    def getStateReachedinMHat(str: Grammar): State = {
//
//      def nextState(s: State, e: Symbol): State = {
//        //println(s"next state: for state $s and symbol $e")
//        MHat.transitionFunction(s, e)
//      }
//
//      str.getSequenceAsList.foldLeft(MHat.iState)(nextState)
//    }
//
//    lazy val prefixes = ce.getAllPrefixes
//    lazy val GammaJ: Grammar =
//      prefixes.find(p => sift(p, DTree).toString != getStateReachedinMHat(p).toString).get
//
//    val gammaJ1               = GammaJ.getSequenceAsList.take(GammaJ.length - 1)
//    val gammaJ1AsGrammar      = Word(gammaJ1.tail, gammaJ1.head)
//    val accessStringToReplace = sift(gammaJ1AsGrammar, DTree)
//    DTree.update(
//      accessStringToReplace,
//      gammaJ1AsGrammar,
//      GammaJ.getSequenceAsList.reverse.head,
//      isMember
//    )
//
//  }
//
//  def learnAutomaton: Automaton = {
//    //Initialization
//    //TODO: is member should take spec
//    val iniState = State(tau.toString)
//    val transitions = A.events.map { a =>
//      Transition(iniState, iniState, a)
//    }
//    val hyp = Automaton("initial", Set(iniState), A, transitions, iniState, None, None)
//    val ce  = ceGen.findCE(Set(t), Set(t), A, hyp, isMember)
//
//    info(hyp)
//    info(ce)
//    val iniTree = ce match {
//      case Left(value)  => Node(t, Leaf(t), Leaf(value))
//      case Right(value) => Node(t, Leaf(t), Leaf(t))
//    }
//    //val iniTree = Node[Grammar](t, Leaf(t), Leaf(ce.left))
//
//    //Main Loop
//    @tailrec
//    def mainLoop(hypTree: DiscriminationTree): Automaton = {
//      val newHyp = makeHypothesis(hypTree)
//      info("tree")
//      info(s"\n $hypTree")
//
//      val newCe =
//        ceGen.findCE(hypTree.getLeafValues, hypTree.getNodeValues, A, newHyp, isMember)
//      info(s"hypothesis: $newHyp")
//      info(s"got CE: $newCe")
//      newCe match {
//        case Left(ce) => mainLoop(updateTree(ce, newHyp, hypTree))
//        case Right(b) => newHyp
//      }
//    }
//    mainLoop(iniTree)
//
//  }
//}
