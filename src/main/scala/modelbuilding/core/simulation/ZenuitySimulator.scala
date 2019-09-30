package modelbuilding.core.simulation
import grizzled.slf4j.Logging
import modelbuilding.core.{Action, Command, Predicate, StateMap}
import com.mathworks.engine.MatlabEngine
import modelbuilding.core.externalClients.ZenuityClient
trait  ZenuitySimulator extends Simulator with Logging {

  override def evalCommandToRun(c: Command, s: StateMap, acceptPartialStates: Boolean): Option[Boolean] = Some(true)

  override def translateCommand(c: Command): List[Action] = ???

  override def runCommand(c: Command, s: StateMap, acceptPartialStates: Boolean): Either[StateMap, StateMap] = ???

  override def runListOfCommands(commands: List[Command], s: StateMap): Either[StateMap, StateMap] = ???
}
