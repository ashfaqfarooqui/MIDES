package CatAndMouse

import modelbuilding.core._
trait CatAndMouseDomain {
  override def toString: String = this match {
    case `c1` => "c1"
    case `c2` => "c2"
    case `c3` => "c3"
    case `c4` => "c4"
    case `c5` => "c5"
    case `c6` => "c6"
    case `c7` => "c7"
    case `m1` => "m1"
    case `m2` => "m2"
    case `m3` => "m3"
    case `m4` => "m4"
    case `m5` => "m5"
    case `m6` => "m6"

  }

}

case object c1 extends Command with CatAndMouseDomain with Controllable
case object c2 extends Command with CatAndMouseDomain with Controllable
case object c3 extends Command with CatAndMouseDomain with Controllable
case object c4 extends Command with CatAndMouseDomain with Controllable
case object c5 extends Command with CatAndMouseDomain with Controllable
case object c6 extends Command with CatAndMouseDomain with Controllable
case object c7 extends Command with CatAndMouseDomain with Uncontrollable
case object m1 extends Command with CatAndMouseDomain with Controllable
case object m2 extends Command with CatAndMouseDomain with Controllable
case object m3 extends Command with CatAndMouseDomain with Controllable
case object m4 extends Command with CatAndMouseDomain with Controllable
case object m5 extends Command with CatAndMouseDomain with Controllable
case object m6 extends Command with CatAndMouseDomain with Controllable

object Occupant extends Enumeration {
  type Occupant = Value
  val CAT, MOUSE, EMPTY = Value
}
