package modelbuilding.externalClients.TraCI

import grizzled.slf4j.Logging
import modelbuilding.core.{Action, Command, Predicate, ResetAction, StateMap, TauAction}
import modelbuilding.core.interfaces.simulator.{Simulator, TwoStateOperation}
import modelbuilding.core._
import org.eclipse.sumo.libsumo.Simulation
import org.eclipse.sumo.libsumo.StringVector

trait TraCISimulator extends Simulator with TwoStateOperation with Logging {



  /** Evaluates if a given command is allowed to be executed on the simulator. This is mainly true for code simulation,
    * and needs to change for external client simulations. A point to think about is if all commands must always be allowed to
    * run in the simulator or not.
    *
    * @param c The command to evaluate
    * @param s current state of the system
    * @param acceptPartialStates
    * @return
    */
  override def evalCommandToRun(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Option[Boolean] = {
    c match {
      case `reset` => Some(true)
      case `tau` => Some(true)
      case x if guards contains x => guards(x).eval(s, acceptPartialStates)
      case y => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  /** Converts the command to a list of actions that will transform the state. These actions are then applied to the
    * state of the simulator.
    *
    * @param c - the command
    * @return List of [[Action]]
    */
  override def translateCommand(c: Command): List[Action] = {
    c match {
      case `reset` => List(ResetAction)
      case `tau` => List(TauAction)
      case x if actions contains x => actions(x)
      case y => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  override def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Either[StateMap, StateMap] = ???

  override def runListOfCommands(
      commands: List[Command],
      s: StateMap
    ): Either[StateMap, StateMap] = ???

}
