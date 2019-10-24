package modelbuilding.core.simulators

import grizzled.slf4j.Logging
import modelbuilding.core.{Action, Command, Predicate, StateMap}
import supremicastuff.SupremicaWatersSystem

trait SupremicaSimulator extends Simulator with Logging {

  override def evalCommandToRun(c: Command, s: StateMap, acceptPartialStates: Boolean): Option[Boolean] = ???

  override def translateCommand(c: Command): List[Action] = ???

  override def runCommand(c: Command, s: StateMap, acceptPartialStates: Boolean): Either[StateMap, StateMap] = ???

  override def runListOfCommands(commands: List[Command], s: StateMap): Either[StateMap, StateMap] = ???
}
