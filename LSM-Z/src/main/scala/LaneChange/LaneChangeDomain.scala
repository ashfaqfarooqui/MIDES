package LaneChange

import modelbuilding.core.{Command, Controllable}

trait LaneChangeDomain {
  override def toString: String = this match {
    case `goRight`       => "goRight"
    case `goLeft`        => "goLeft"
    case `cancelRequest` => "cancelRequest"
    case `b11true`       => "b11true"
    case `b12true`       => "b12true"
    case `b3true`        => "b3true"
    case `b4true`        => "b4true"
    case `b5true`        => "b5true"
    case `b6true`        => "b6true"
    case `b7true`        => "b7true"
    case `b11false`      => "b11false"
    case `b12false`      => "b12false"
    case `b3false`       => "b3false"
    case `b4false`       => "b4false"
    case `b5false`       => "b5false"
    case `b6false`       => "b6false"
    case `b7false`       => "b7false"
    case `b8false`       => "b8false"
    case `b9false`       => "b9false"
    case `b10false`      => "b10false"
    case `b8true`        => "b8true"
    case `b9true`        => "b9true"
    case `b10true`       => "b10true"
  }
}

case object goRight       extends Command with LaneChangeDomain with Controllable
case object goLeft        extends Command with LaneChangeDomain with Controllable
case object cancelRequest extends Command with LaneChangeDomain with Controllable
case object b11true       extends Command with LaneChangeDomain with Controllable
case object b12true       extends Command with LaneChangeDomain with Controllable
case object b3true        extends Command with LaneChangeDomain with Controllable
case object b4true        extends Command with LaneChangeDomain with Controllable
case object b5true        extends Command with LaneChangeDomain with Controllable
case object b6true        extends Command with LaneChangeDomain with Controllable
case object b7true        extends Command with LaneChangeDomain with Controllable
case object b11false      extends Command with LaneChangeDomain with Controllable
case object b12false      extends Command with LaneChangeDomain with Controllable
case object b3false       extends Command with LaneChangeDomain with Controllable
case object b4false       extends Command with LaneChangeDomain with Controllable
case object b5false       extends Command with LaneChangeDomain with Controllable
case object b6false       extends Command with LaneChangeDomain with Controllable
case object b7false       extends Command with LaneChangeDomain with Controllable
case object b8false       extends Command with LaneChangeDomain with Controllable
case object b9false       extends Command with LaneChangeDomain with Controllable
case object b10false      extends Command with LaneChangeDomain with Controllable
case object b8true        extends Command with LaneChangeDomain with Controllable
case object b9true        extends Command with LaneChangeDomain with Controllable
case object b10true       extends Command with LaneChangeDomain with Controllable

sealed trait Request {
  override def toString: String = {
    this match {
      case LEFT   => "left"
      case RIGHT  => "right"
      case CANCEL => "none"
    }
  }
}
case object LEFT   extends Request
case object RIGHT  extends Request
case object CANCEL extends Request
