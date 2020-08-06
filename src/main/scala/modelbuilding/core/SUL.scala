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

package modelbuilding.core

import modelbuilding.helpers.Statistics
import grizzled.slf4j.Logging
import modelbuilding.core.interfaces.algorithms.Teacher
import modelbuilding.core.interfaces.modeling.{Model, Specifications}
import modelbuilding.core.interfaces.simulator.Simulator
import org.supremica.automata

import scala.collection.JavaConverters._

object SUL {
  def apply(
      model: Model,
      simulator: Simulator,
      specification: Option[Specifications],
      acceptsPartialStates: Boolean
    ): SUL = new SUL(model, simulator, specification, acceptsPartialStates)
}

/**
  * The SUL stands for System Under Learning and it is that that is exposed to the learning algorithm.
  * @param model - A [[Model]] defining the overall system.
  * @param simulator - A accessible [[Simulator]]
  * @param specification - If the [[SUL]] has any [[Specifications]] they are defined here.
  * @param acceptsPartialStates - A flag to say if guard evaluations need to be done on the partial sate.
  */
case class SUL(
    model: Model,
    simulator: Simulator,
    specification: Option[Specifications],
    acceptsPartialStates: Boolean = false)
    extends Teacher
    with Logging {

  var simQueries: Int = 0

  val statistics = new Statistics
  def getInitState: StateMap               = simulator.initState
  def getGoalStates: Option[Set[StateMap]] = simulator.goalStates
  def getGoalPredicate: Option[Predicate]  = simulator.goalPredicate

  /**
    * Returns the next state reached from the given state when performing the command in the simulator.
    * If the command is nto possible we return a None.
    */
  def getNextState(state: StateMap, command: Command): Option[StateMap] = {
    val resp = simulator.runCommand(command, state, acceptsPartialStates) match {
      case Right(s) => Some(s)
      case Left(_)  => None
    }
    info(s"got response $resp ")
    simQueries += 1
    resp
  }

  def getNextState(state: StateMap, commands: Alphabet): List[StateMap] =
    getOutgoingTransitions(state, commands).map(_.target)

  def getOutgoingTransitions(
      state: StateMap,
      commands: Alphabet
    ): List[StateMapTransition] =
    commands.events.foldLeft(List.empty[StateMapTransition])(
      (acc: List[StateMapTransition], in: Symbol) =>
        getNextState(state, in.getCommand) match {
          case Some(s) => StateMapTransition(state, s, in) :: acc
          case None    => acc
        }
    )

  def grammarToList(g: Grammar): List[Command] = {
    g match {
      case w: Word   => w.getSequence.map(_.getCommand)
      case s: Symbol => List(s.getCommand)
    }
  }

  def executeSequence(seq: Grammar): Option[StateMap] =
    simulator.runListOfCommands(grammarToList(seq), getInitState) match {
      case Right(st) => Some(st)
      case Left(st)  => None
    }

  /**
    * Check if the given sequence is allowed by the specification
    * @param sequence
    * @param specName
    * @return
    */
  //Check-> send in a function to evaluate additionally. here I use if to check accepting nature of the reached state
  def isSequenceAllowedInSpec(
      sequence: Grammar,
      specName: String
    ): Either[Boolean, automata.State] = {

    val specAutomaton = specification.get.getSupremicaSpecs(specName)
    val s             = specAutomaton.getInitialState
    val alphabet      = specAutomaton.getAlphabet
    val p = sequence.getSequenceAsString
      .filterNot(_ == tau.toString)
      .filter(a => alphabet.contains(a.toString))

    @scala.annotation.tailrec
    def loop(s: automata.State, p: List[String]): Either[Boolean, automata.State] = {
      //debug(s"inloop: at $s and ${p}")
      //debug(s"check ${check(s)}")
      if (p.nonEmpty && s.getOutgoingArcs.asScala.exists(_.getEvent.getLabel == p.head)) {
        loop(
          s.getOutgoingArcs.asScala.find(_.getEvent.getLabel == p.head).get.getToState,
          p.tail
        )
      } else if (p.isEmpty) Right(s)
      else {
        Left(false)
      }
    }
    val allowedInSpec = loop(s, p)

    debug(s"is sequence $sequence allowed in spec $allowedInSpec")
    allowedInSpec
  }

  /**
    * Called by the isMember function, here we check if the given sequence is uncontrollable.
    * @param sequence - Same as 'g' in the other places.
    * @param alphabet - model.Alphabet
    * @param specName - The name of the spec to check controllability with
    * @return - True if sequence is uncontrollable wrt plant,  
    */
  def isSequenceUnControllableInSpec(
      sequence: Grammar,
      alphabet: Alphabet,
      specName: String
    ): Boolean = {

    val specAutomaton = specification.get.getSupremicaSpecs(specName)
    val pref          = sequence.getAllPrefixes
    val ucEvents      = alphabet.events.filter(_.getCommand.isInstanceOf[Uncontrollable])
    val su            = ucEvents //.flatMap(a=>ucEvents.map(a+_)).flatMap(a=>ucEvents.map(a+_))
    val tu            = pref.flatMap(t => su.map(t + _))
    //val tuNotCtrl = tu.filterNot(x=>isSequenceAllowedInSpec(x,specName,_=>true)).exists(executeSequence(_).isDefined)

    val tuNotCtrl = tu
      .filterNot(x => isSequenceAllowedInSpec(x, specName).isRight)
      .exists(executeSequence(_).isDefined)
    debug(s"is sequence $sequence controllable $tuNotCtrl")
    tuNotCtrl
  }

  /**
    * Subfunction that is used by isMember to check if the given sequence is allowed by the [[SUL]].
    * @param sequence
    * @return 0 - if not allowed, 1 - if allowed but does not reach a goal state, 2 - if reaches a goal state
    */
  def isAllowedInPlant(sequence: Grammar): Int = {
    var returnValue: Int = -1
    def evalGoal(st: StateMap) = {
      getGoalPredicate.getOrElse(AlwaysTrue).eval(st).get || getGoalStates
        .getOrElse(Set.empty)
        .contains(st)
    }
    val resultState = executeSequence(sequence)
    if (resultState.isDefined) {
      if (evalGoal(resultState.get))
        returnValue = 2
      else returnValue = 1
    } else returnValue = 0
//Include specs
    debug(s"is $sequence allowed in plant $returnValue")
    returnValue
  }

  /**
    * This function is used to check if a given string is allowed by the SUL; and is used
   * by the [[LStar]] algorithm.
    * @param specName - Name of spec to include, if needed.
    * @param sequence - Sequence of events that need to be checked. 
    * @return - 0,1, or 2.
    */
  def isMember(specName: Option[String])(sequence: Grammar): Int = {
    
    statistics.membQueries = statistics.membQueries + 1
    val member = if (specName.isDefined) {
      if (!isSequenceUnControllableInSpec(sequence, model.alphabet, specName.get)) {
        lazy val specAllowed = isSequenceAllowedInSpec(sequence, specName.get) match {
          case Right(value) =>
            if (specification.get.isAccepting(specName.get, value.getName)) 2 else 1
          case Left(value) => 0
        }
        isAllowedInPlant(sequence) min specAllowed
      } else 0
    } else isAllowedInPlant(sequence) //Plant solver
    debug(s"is $sequence member $member")
    member
  }

}
