package modelbuilding.core.simulation

import grizzled.slf4j.Logging
import modelbuilding.core.modelInterfaces.Teacher
import modelbuilding.core.{Alphabet, AlwaysTrue, Command, Grammar, Predicate, StateMap, StateMapTransition, Symbol, Word}

abstract class SUL extends Teacher with Logging{

  val simulator: Simulator
  val acceptsPartialStates: Boolean = false

  def getInitState: StateMap = simulator.initState
  def getGoalStates: Option[Set[StateMap]] = simulator.goalStates
  def getGoalPredicate: Option[Predicate] = simulator.goalPredicate

  def getNextState(state: StateMap, command:Command): Option[StateMap] = {
    simulator.runCommand(command, state, acceptsPartialStates) match {
      case Right(s) => Some(s)
      case Left(_) => None
    }
  }

  def getNextState(state:StateMap, commands: Alphabet): List[StateMap] =
    getOutgoingTransitions(state, commands).map(_.target)

  def getOutgoingTransitions(state: StateMap, commands: Alphabet): List[StateMapTransition] =
    commands.events.foldLeft(List.empty[StateMapTransition])((acc: List[StateMapTransition], in: Symbol) =>
      getNextState(state, in.getCommand) match {
        case Some(s) => StateMapTransition(state, s, in) :: acc
        case None => acc
      })

  def grammarToList(g: Grammar):List[Command]={
    g match {
      case w : Word => w.getSequence.map(_.getCommand)
      case s : Symbol => List(s.getCommand)
    }
  }

  def isMember(g:Grammar):Int= {

    val cmds = grammarToList(g)

    simulator.runListOfCommands(cmds, getInitState) match {

      case Right(st) => if(getGoalPredicate.getOrElse(AlwaysTrue).eval(st).get ||getGoalStates.getOrElse(Set.empty).contains(st)) 2 else 1
      case Left(st) =>
        0
    }
  }


}
