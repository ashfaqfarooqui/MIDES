package modelbuilding.models.RobotArm

import modelbuilding.core._
trait ArmDomain {
  override def toString: String = this match {
    case `left` => "l"
    case `right` =>"r"
    case `up` => "u"
    case `down` =>"d"
    case `extend` =>"extend"
    case `retract` =>"retract"
    case `grip` =>"grip"
    case `release` =>"release"

  }
}

case object left extends Command with ArmDomain with Controllable
case object right extends Command with ArmDomain with Controllable
case object up extends Command with ArmDomain with Controllable
case object down extends Command with ArmDomain with Controllable
case object extend extends Command with ArmDomain with Controllable
case object retract extends Command with ArmDomain with Controllable
case object grip extends Command with ArmDomain with Controllable
case object release extends Command with ArmDomain with Controllable
