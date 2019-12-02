package modelbuilding.core.simulators

import Helpers.ConfigHelper
import grizzled.slf4j.Logging
import modelbuilding.core._
import modelbuilding.core.externalClients.MiloOPCUAClient

trait OPCVariables {
  val stateExecVariable: String
  val stateExecFinishedValue: String

}

trait OPCSimulator
    extends Simulator
    with OPCVariables
    with ThreeStateOperation
    with Logging {

  private var opcClient: Option[MiloOPCUAClient] = None

  //opcClient.connect()

  def getClient = {
    if (opcClient.isEmpty) opcClient = Some(MiloOPCUAClient())
    opcClient.get
  }

  def subscribeToAllVars = {
    //val v = variableList.get.map(_._1)

    println(s"client $getClient")
    //getClient.subscribeToNodes(List("GVL.R1", "GVL.R2", "GVL.R3", "GVL.R4", "GVL.S1", "GVL.S2", "GVL.S3", "GVL.S4", "GVL.S5", "GVL.RESET"))    //Nodes for MB
    getClient.subscribeToNodes(List("GVL.R1", "GVL.R2", "GVL.R3", "GVL.R4", "GVL.R5", "GVL.R6", "GVL.R7", "GVL.S1", "GVL.S2", "GVL.S3", "GVL.S4", "GVL.S5", "GVL.S6", "GVL.S7", "GVL.S8", "GVL.RESET")) //Nodes for TL
  }

  /**
    * Use this function to reset the system.
    * @return
    */
  def resetSystem = {
    getClient.write("GVL.RESET",true)
  }

  def initializeSystem = {
    getClient.connect()
    while(!getClient.isConnected){}
    subscribeToAllVars

    resetSystem
    println("The system has initialized.")
  }

  /**
    * Reset the system, wait for sometime before reading the state variables.
    * @return
    */
  def getInitialState = {
    initializeSystem
    //resetSystem
    getClient.getState
  }

  override val initState: StateMap = getInitialState

  override def evalCommandToRun(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Option[Boolean] = {
    c match {
      case `reset`                => Some(true)
      case `tau`                  => Some(true)
      case x if guards contains x => guards(x).eval(s, acceptPartialStates)
      case y                      => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  override def translateCommand(c: Command): List[Action] =

    c match {
      case `reset`                 => List(ResetAction)
      case `tau`                   => List(TauAction)
      case x if actions contains x => actions(x)
      case y                       => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }

  def isCommandDone(c: Command,acceptPartialState:Boolean) = {
    //println(s"evaluating  command $c, with guard ${postGuards(c)} and statemap ${getClient.getState}")
    postGuards(c).eval(getClient.getState,acceptPartialState).get
  }

  override def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Either[StateMap, StateMap] = {
    evalCommandToRun(c, s, acceptPartialStates) match {
      case Some(true) =>
        //println(s"running command $c")

        c match {
          case `reset` =>
            resetSystem
            println("The system has reset now.")
            Right(getClient.getState)
          case `tau` =>
            //Thread.sleep(100)
            //println(s"command $c executed.")
            Right(getClient.getState)
          case _ =>
            getClient.setState(s)

            val currState = translateCommand(c).foldLeft(getClient.getState) {
              (acc, ac) =>
                ac.next(acc)
            }
            getClient.setState(currState)
            import scala.concurrent.duration._

            val deadline = 10.seconds.fromNow
            while (!isCommandDone(c,acceptPartialStates) && deadline.hasTimeLeft()) {
              //println(s"command $c executed, wait for simulation to complete.")
              //Thread.sleep(1000)
            }
            if (deadline.hasTimeLeft()) {
              println(s"command $c is finished, wait to set postactions.")
              Thread.sleep(ConfigHelper.runner_timeout)
              val newState = postActions(c).foldLeft(getClient.getState) { (acc, ac) =>
                ac.next(acc)
              }
              getClient.setState(newState)
              //println("Postactions are set.")
              Thread.sleep(ConfigHelper.runner_timeout)
              Right(getClient.getState)
            }else{
              println(s"Error: command $c has not finished in time, wait for return.")
              info(s"Command $c has not finished in time, stop simulation.")
              Thread.sleep(ConfigHelper.runner_timeout)
              System.exit(1)
              val newState = postActions(c).foldLeft(getClient.getState) { (acc, ac) =>
                ac.next(acc)
              }
              getClient.setState(newState)
              //println("Postactions are set.")
              Thread.sleep(ConfigHelper.runner_timeout)
              Left(getClient.getState)
            }
        }
      case Some(false) => Left(getClient.getState)
      case None =>
        throw new IllegalArgumentException(
          s"Can not evaluate Command `$c`, since Partial state `$s` does not include all affected variables."
        )
    }

  }

  override def runListOfCommands(
      commands: List[Command],
      s: StateMap
    ): Either[StateMap, StateMap] = {
resetSystem
    println("The system has reset.")
    Thread.sleep(ConfigHelper.runner_timeout)
    def runList(c: List[Command], ns: StateMap): Either[StateMap, StateMap] = {

      c match {
        case x :: xs =>
          //println(s"running element of the list $c ")

          runCommand(x, ns) match {
            case Right(n) => runList(xs, n)
            case Left(n)  => Left(n)
          }
        case Nil => Right(ns)
      }

    }
    println(s"running list $commands")
    val returnvalue = runList(commands, s)
    if (returnvalue.isLeft) {
      debug(s"We got a return value for $commands: ${returnvalue}")

    }
    returnvalue

  }

}
