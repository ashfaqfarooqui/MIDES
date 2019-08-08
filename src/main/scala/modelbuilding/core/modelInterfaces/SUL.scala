package modelbuilding.core.modelInterfaces

import modelbuilding.core.{Alphabet, Command, StateMap, Symbol, StateMapTransition}

abstract class SUL {

  val simulator: Simulator
  val stateToString: StateMap => Option[String] =
    (s: StateMap) => {
      val name =
        (if (s.state.forall{
          case (k,v) => getInitState.state(k) == v
        }) "INIT: " else "") +
        s.state.map(v => s"(${v._1}=${v._2})").mkString(",")
      Some(name)
    }

  def getInitState: StateMap = simulator.initState
  def getGoalStates: Option[Set[StateMap]] = simulator.goalStates

  def getNextState(state: StateMap, command:Command): Option[StateMap] = {
    simulator.runCommand(command, state) match {
      case Right(s) => Some(s)
      case Left(_) => None
    }
  }

  def getNextState(state:StateMap, commands: Alphabet): Set[StateMap] =
    getOutgoingTransitions(state, commands).map(_.target)

  def getOutgoingTransitions(state:StateMap, commands: Alphabet): Set[StateMapTransition] =
    commands.a.foldLeft(List.empty[StateMapTransition])((acc: List[StateMapTransition], in: Symbol) =>
      getNextState(state, in.getCommand) match {
        case Some(s) => StateMapTransition(state, s, in) :: acc
        case None => acc
      }).toSet

}
