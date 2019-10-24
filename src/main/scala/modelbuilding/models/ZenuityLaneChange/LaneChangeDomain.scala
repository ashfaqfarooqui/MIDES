package modelbuilding.models.ZenuityLaneChange

import modelbuilding.core.{Command, Controllable}

trait LaneChangeDomain {
  override def toString: String = this match {
    case `goRight` => "goRight"
    case `goLeft` => "goLeft"
    case `cancelRequest` => "cancelRequest"
    case `b1true` => "b1true"
    case `b2true` => "b2true"
    case `b3true` => "b3true"
    case `b4true` => "b4true"
    case `b5true` => "b5true"
    case `b6true` => "b6true"
    case `b7true` => "b7true"
    case `b1false` => "b1false"
    case `b2false` => "b2false"
    case `b3false` => "b3false"
    case `b4false` => "b4false"
    case `b5false` => "b5false"
    case `b6false` => "b6false"
    case `b7false` => "b7false"
  }
}

case object  goRight extends Command with LaneChangeDomain with Controllable
case object  goLeft extends Command with LaneChangeDomain with Controllable
case object  cancelRequest extends Command with LaneChangeDomain with Controllable
case object  b1true extends Command with LaneChangeDomain with Controllable
case object  b2true extends Command with LaneChangeDomain with Controllable
case object  b3true extends Command with LaneChangeDomain with Controllable
case object  b4true extends Command with LaneChangeDomain with Controllable
case object  b5true extends Command with LaneChangeDomain with Controllable
case object  b6true extends Command with LaneChangeDomain with Controllable
case object  b7true extends Command with LaneChangeDomain with Controllable
case object  b1false extends Command with LaneChangeDomain with Controllable
case object  b2false extends Command with LaneChangeDomain with Controllable
case object  b3false extends Command with LaneChangeDomain with Controllable
case object  b4false extends Command with LaneChangeDomain with Controllable
case object  b5false extends Command with LaneChangeDomain with Controllable
case object  b6false extends Command with LaneChangeDomain with Controllable
case object b7false extends Command with LaneChangeDomain with Controllable


sealed trait Request
case object LEFT extends Request
case object RIGHT extends Request
case object CANCEL extends Request
