package StickPicking

import modelbuilding.core.{Command, Controllable, Uncontrollable}

trait SticksDomain {
  override def toString: String = this match {
    case `e11` => "e1:1"
    case `e21` => "e2:1"
    case `e12` => "e1:2"
    case `e22` => "e2:2"
    case `e13` => "e1:3"
    case `e23` => "e2:3"
  }
}

case object e11 extends Command with SticksDomain with Controllable
case object e21 extends Command with SticksDomain with Uncontrollable
case object e12 extends Command with SticksDomain with Controllable
case object e22 extends Command with SticksDomain with Uncontrollable
case object e13 extends Command with SticksDomain with Controllable
case object e23 extends Command with SticksDomain with Uncontrollable
