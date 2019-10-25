package modelbuilding.core.simulators
import grizzled.slf4j.Logging
import modelbuilding.core._
import com.mathworks.engine.MatlabEngine
import modelbuilding.core.externalClients.ZenuityClient
trait ZenuitySimulator extends Simulator with TwoStateOperation with Logging {

  override def evalCommandToRun(c: Command, s: StateMap, acceptPartialStates: Boolean): Option[Boolean] = Some(true)

  override def translateCommand(c: Command): List[Action] = {
    c match {
      case `reset`                 => List(ResetAction)
      case `tau`                   => List(TauAction)
      case x if actions contains x => actions(x)
      case y                       => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  override def runCommand(c: Command, s: StateMap, acceptPartialStates: Boolean): Either[StateMap, StateMap] = {

    ???
  }

  override def runListOfCommands(commands: List[Command], s: StateMap): Either[StateMap, StateMap] = ???
}
