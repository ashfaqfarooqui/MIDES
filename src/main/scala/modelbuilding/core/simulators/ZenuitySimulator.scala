package modelbuilding.core.simulators
import grizzled.slf4j.Logging
import modelbuilding.core._
import com.mathworks.engine.MatlabEngine
import modelbuilding.core.externalClients.{MiloOPCUAClient, ZenuityClient}
trait ZenuitySimulator extends Simulator with TwoStateOperation with Logging {

  private var matlabClient: Option[ZenuityClient] = None

  def getClient = {
    if (matlabClient.isEmpty) matlabClient = Some(ZenuityClient())
    matlabClient.get
  }

  //TODO: put back reset
  def getInitialState = {
    // getClient.reset
    getClient.getState
  } //.getEngine.feval(3, "gcd", Int.box(40), Int.box(60))

  override def evalCommandToRun(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Option[Boolean] = Some(true)

  override def translateCommand(c: Command): List[Action] = {
    c match {
      case `reset`                 => List(ResetAction)
      case `tau`                   => List(TauAction)
      case x if actions contains x => actions(x)
      case y                       => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  override def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Either[StateMap, StateMap] = {

    evalCommandToRun(c, s, acceptPartialStates) match {
      case Some(true) =>
        info(s"running command $c")

        c match {
          case `reset` =>
            getClient.reset
            Right(getClient.getState)
          case `tau` => Right(getClient.getState)
          case _ =>
            getClient.setState(s)

            val currState = translateCommand(c).foldLeft(getClient.getState) {
              (acc, ac) =>
                ac.next(acc)
            }
            getClient.runProgram(currState)

        }
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
          //println(s"running element of the list $c ")

          runCommand(x, ns) match {
            case Right(n) => runList(xs, n)
            case Left(n)  => Left(n)
          }
        case Nil => Right(ns)
      }

    }
    println(s"running list $commands")
    runList(commands :+ `reset`, s)

  }
}
