package modelbuilding.core.simulation
import grizzled.slf4j.Logging
import modelbuilding.core._
import modelbuilding.core.externalClients.MiloOPCUAClient


trait OPCSimulator extends Simulator with Logging{


  val opcClient = MiloOPCUAClient()
  opcClient.connect()

  /**
   * Use this function to reset the system.
   * @return
   */
  def resetSystem = ???


  /**
   * Reset the system, wait for sometime before reading the state variables.
   * @return
   */
  def getInitialState = {
    resetSystem
    wait(2)
    opcClient.getState
  }

  override val initState: StateMap = getInitialState

  override def evalCommandToRun(c: Command, s: StateMap, acceptPartialStates: Boolean): Option[Boolean] = Some(true) //Always allow eval command


  override  def translateCommand(c: Command): List[Action] =
    c match {
      case `reset` => List(ResetAction)
      case `tau` => List(TauAction)
      /*** if guards contains x => actions(x), in this simulator we
       * should not worry about the gaurds. All events are always assumed
       * executable, only the simulation can turn it off
       */
      case x => actions(x) //
      case y => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }


  override def runCommand(c: Command, s: StateMap, acceptPartialStates: Boolean): Either[StateMap, StateMap] = {
    opcClient.setState(s)

    val currState = translateCommand(c).foldLeft(opcClient.getState){
      (acc,ac) => ac.next(acc)
    }
    opcClient.setState(currState)
    while(opcClient.getState.getOrElse("status","None").toString!="Finished"){}
    ???
  }

  override def runListOfCommands(commands: List[Command], s: StateMap): Either[StateMap, StateMap] = ???
}
