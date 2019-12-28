package modelbuilding.core.simulators
import modelbuilding.core._

import grizzled.slf4j.Logging
import modelbuilding.core.{Action, Assign, Command, Predicate, StateMap, TauAction}

trait CodeSimulator extends Simulator with TwoStateOperation with Logging {

  override val initState: StateMap
  override val goalStates: Option[Set[StateMap]]
  override val goalPredicate: Option[Predicate] = None

  def evalCommandToRun(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean = false
    ): Option[Boolean] =
    c match {
      case `reset`                => Some(true)
      case `tau`                  => Some(true)
      case x if guards contains x => guards(x).eval(s, acceptPartialStates)
      case y                      => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }

  def translateCommand(c: Command): List[Action] =
    c match {
      case `reset`                => initState.getState.toList.map(x => Assign(x._1, x._2))
      case `tau`                  => List(TauAction)
      case x if guards contains x => actions(x)
      case y                      => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }

  def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean = false
    ): Either[StateMap, StateMap] =
    evalCommandToRun(c, s, acceptPartialStates) match {
      case Some(true)  => Right(translateCommand(c).foldLeft(s)((st, a) => a.next(st)))
      case Some(false) => Left(s)
      case None =>
        throw new IllegalArgumentException(
          s"Can not evaluate Command `$c`, since Partial state `$s` does not include all affected variables."
        )
    }

  def runListOfCommands(
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
