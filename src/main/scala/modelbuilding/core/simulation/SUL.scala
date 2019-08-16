package modelbuilding.core.simulation

import modelbuilding.core.{Alphabet, Command, Predicate, StateMap, StateMapTransition, Symbol}

abstract class SUL {

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

}