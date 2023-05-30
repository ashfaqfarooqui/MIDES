package LCTraci

import modelbuilding.core.{Command, Controllable}

trait LCTraciDomain {

  override def toString: String = this match {
    case `normalStep`  => "normalStep"
    case `faultyStep`  => "faultyStep"
  }
}

case object normalStep  extends Command with LCTraciDomain with Controllable
case object faultyStep  extends Command with LCTraciDomain with Controllable
