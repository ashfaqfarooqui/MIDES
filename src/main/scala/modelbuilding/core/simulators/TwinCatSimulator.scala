/*package modelbuilding.core.simulators
import modelbuilding.core._
import grizzled.slf4j.Logging
import modelbuilding.core.externalClients.BeckhoffADS
import modelbuilding.core.{Action, Command, Predicate, StateMap}
import nl.vroste.adsclient.AdsCodecs

trait TwinCatSimulator extends Simulator with ThreeStateOperation with Logging {

  private var client: Option[BeckhoffADS] = None

  def getClient = {
    if (client.isEmpty) client = Some(BeckhoffADS())
    client.get
  }

  def resetSystem = ???

  implicit def stringToCodec(s: String) = {
    s.toLowerCase match {
      case "int"     => AdsCodecs.int
      case "string"  => AdsCodecs.string
      case "boolean" => AdsCodecs.bool
      case _         => throw new NoSuchElementException(s"codec $s is not implemented")
    }
  }
  def subscribeToAllVars = {
    variableList.get.foreach(x => getClient.subscribeTo(x._1, x._2))
  }

  def initializeSystem = {
    getClient.getAdsClient

  }

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

  /**
 * Converts the command to a list of actions that will transform the state. These actions are then applied to the
 * state of the simulator.
 *
 * @param c - the command
 * @return List of [[Action]]
 */
  override def translateCommand(c: Command): List[Action] = c match {
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
      case Some(false) => Left(s)
      case None =>
        throw new IllegalArgumentException(
          s"Can not evaluate Command `$c`, since Partial state `$s` does not include all affected variables."
        )
    }
  }

  override def runListOfCommands(
      commands: List[Command],
      s: StateMap
    ): Either[StateMap, StateMap] = ???

}
 */
