package modelbuilding.models.TestUnit

import modelbuilding.core._
trait TLDomain {
  override def toString: String = this match {
    case `start1` => "s1"
    case `finish1` =>"f1"
    case `start2` => "s2"
    case `finish2` =>"f2"
    case `test` =>"l"
    case `accept` =>"a"
    case `reject` =>"r"

  }
}

case object start1 extends Command with TLDomain with Controllable
case object finish1 extends Command with TLDomain with Uncontrollable
case object start2 extends Command with TLDomain with Controllable
case object finish2 extends Command with TLDomain with Uncontrollable
case object accept extends Command with TLDomain with Uncontrollable
case object reject extends Command with TLDomain with Uncontrollable
case object test extends Command with TLDomain with Controllable


object Status extends Enumeration {
  type Status = Value
  val Initial, Working = Value
}