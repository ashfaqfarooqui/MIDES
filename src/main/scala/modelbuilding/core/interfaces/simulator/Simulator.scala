package modelbuilding.core.interfaces.simulator

import grizzled.slf4j.Logging
import modelbuilding.core._

/** This is a trait for the simulator. All simulators must extend from this trait. Since the [[SUL]]
  * accepts a simulator as an input
  */
trait Simulator extends Logging {

  /** List of variables to subscribe on the PLC end. Use this with OPC communication */
  val variableList: Option[List[(String, String)]] = None

  val initState: StateMap
  val goalStates: Option[Set[StateMap]]
  val goalPredicate: Option[Predicate] = None

  /**
    * Evaluates if a given command is allowed to be executed on the simulator. This is mainly true for code simulation,
    * and needs to change for external client simulations. A point to think about is if all commands must always be allowed to
    * run in the simulator or not.
    *
    * @param c The command to evaluate
    * @param s current state of the system
    * @param acceptPartialStates
    * @return
    */
  def evalCommandToRun(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean = false
    ): Option[Boolean]

  /**
    * Converts the command to a list of actions that will transform the state. These actions are then applied to the
    * state of the simulator.
    *
    * @param c - the command
    * @return List of [[Action]]
    */
  def translateCommand(c: Command): List[Action]

  def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean = false
    ): Either[StateMap, StateMap]

  def runListOfCommands(commands: List[Command], s: StateMap): Either[StateMap, StateMap]
}
