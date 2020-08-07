package modelbuilding.core.interfaces.simulator

import grizzled.slf4j.Logging
import modelbuilding.core.{Action, Assign, Command, Predicate, StateMap, TauAction, _}

/**
  * This trait defines functions run a code based simulator. Here the simulation model is created using variables and thier transitions in the system.
  *
  */
trait TimedCodeSimulator extends CodeSimulator with Logging {

  type Duration = Double
  def calculateDuration(transition: StateMapTransition): Duration
//  def runTimedCommand(
//      c: Command,
//      s: StateMap,
//      acceptPartialStates: Boolean = false
//    ): Either[(Duration, StateMap), (Duration, StateMap)] =
//    runCommand(c,s,acceptPartialStates) match {
//      case Right(t)  => Right((calculateDuration(s,c,t),t))
//      case Left(t) => Left((0,t))
//    }

}
