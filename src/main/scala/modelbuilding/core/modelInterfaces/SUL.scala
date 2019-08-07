package modelbuilding.core.modelInterfaces

import modelbuilding.core.{Alphabet, Command, StateMap, Symbol, Transition}

abstract class SUL {

  val simulator:Simulator
  def getNextState(state: StateMap, command:Command): Option[StateMap] = {
    simulator.runCommand(command, state) match {
      case Right(s) => Some(s)
      case Left(_) => None
    }
  }
  def getNextState(state:StateMap, commands: Alphabet): Set[Transition] = {

    commands.a.foldLeft(List.empty[Transition])((acc: List[Transition], in: Symbol) =>
      getNextState(state, in.getCommand) match {
        case Some(s) => Transition(state, s, in.getCommand) :: acc
        case None => acc
      }).toSet


  }
}
