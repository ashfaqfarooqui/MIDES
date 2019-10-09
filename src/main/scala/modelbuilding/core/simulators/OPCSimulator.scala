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
    getClient.subscribeToNodes(variableList.get.map(_._1))
  }

  /**
    * Use this function to reset the system.
    * @return
    */
  def resetSystem = ???

  def initializeSystem = {
    getClient.connect()
    subscribeToAllVars

  }

  /**
    * Reset the system, wait for sometime before reading the state variables.
    * @return
    */
  def getInitialState = {
    initializeSystem
    resetSystem
    wait(2)
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

  def isCommandDone(c: Command) =
    postGuards(c).eval(getClient.getState).get
  override def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Either[StateMap, StateMap] = {
    evalCommandToRun(c, s, acceptPartialStates) match {
      case Some(true) =>
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

            val deadline = 5.seconds.fromNow
            while (!isCommandDone(c) && deadline.hasTimeLeft()) {
              wait(1)
            }
            if (deadline.hasTimeLeft()) {
              wait(1)
              postActions(c).foldLeft(getClient.getState) { (acc, ac) =>
                ac.next(acc)
              }
              getClient.setState(currState)
              Right(getClient.getState)
            }
            Left(getClient.getState)
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

    def runList(c: List[Command], ns: StateMap): Either[StateMap, StateMap] = {

      c match {

        case x :: xs =>
          runCommand(x, ns) match {
            case Right(n) => runList(xs, n)
            case Left(n)  => Left(n)
          }
        case Nil => Right(ns)
      }

    }
    runList(commands, s)

  }

}
