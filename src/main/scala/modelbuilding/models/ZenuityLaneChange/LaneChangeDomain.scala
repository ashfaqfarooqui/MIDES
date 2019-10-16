package modelbuilding.models.ZenuityLaneChange

import modelbuilding.core.{Command, Controllable}
import scala.collection.JavaConverters._

trait LaneChangeDomain {
  override def toString: String = this match {
    case `goRight`       => "goRight"
    case `goLeft`        => "goLeft"
    case `cancelRequest` => "cancelRequest"
    case `b1true`        => "b1true"
    case `b2true`        => "b2true"
    case `b3true`        => "b3true"
    case `b4true`        => "b4true"
    case `b5true`        => "b5true"
    case `b6true`        => "b6true"
    case `b7true`        => "b7true"
    case `b1false`       => "b1false"
    case `b2false`       => "b2false"
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
case object b1true        extends Command with LaneChangeDomain with Controllable
case object b2true        extends Command with LaneChangeDomain with Controllable
case object b3true        extends Command with LaneChangeDomain with Controllable
case object b4true        extends Command with LaneChangeDomain with Controllable
case object b5true        extends Command with LaneChangeDomain with Controllable
case object b6true        extends Command with LaneChangeDomain with Controllable
case object b7true        extends Command with LaneChangeDomain with Controllable
case object b1false       extends Command with LaneChangeDomain with Controllable
case object b2false       extends Command with LaneChangeDomain with Controllable
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
