package modelbuilding.core.simulators
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
    getClient.subscribeToNodes(List("GVL.R1","GVL.R2", "GVL.R3", "GVL.R4",
      "GVL.Load_R1_initial","GVL.Load_R1_execute","GVL.Load_R1_finish",
      "GVL.Unload_R1_initial","GVL.Unload_R1_execute","GVL.Unload_R1_finish",
      "GVL.Load_R2_initial","GVL.Load_R2_execute","GVL.Load_R2_finish",
      "GVL.Unload_R2_initial","GVL.Unload_R2_execute","GVL.Unload_R2_finish", "GVL.RESET"))
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
    println(s"evaluating  command $c, with guard ${postGuards(c)} and statemap ${getClient.getState}")
    postGuards(c).eval(getClient.getState,acceptPartialState).get
  }

  override def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Either[StateMap, StateMap] = {
    evalCommandToRun(c, s, acceptPartialStates) match {
      case Some(true) =>
        println(s"running command $c")

        c match {
          case `reset` =>
            resetSystem
            Right(getClient.getState)
          case `tau` => Right(getClient.getState)
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
              println("waiting....")
              Thread.sleep(6000)
            }
            if (deadline.hasTimeLeft()) {
              Thread.sleep(6000)
              val newState = postActions(c).foldLeft(getClient.getState) { (acc, ac) =>
                ac.next(acc)
              }
              getClient.setState(newState)
              Thread.sleep(6000)
              Right(getClient.getState)
            }else{

              Thread.sleep(5000)
              val newState = postActions(c).foldLeft(getClient.getState) { (acc, ac) =>
                ac.next(acc)
              }
              getClient.setState(newState)
              Thread.sleep(6000)
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
    runList(commands, s)

  }

}
