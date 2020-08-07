package modelbuilding.models.WeldingRobots

import modelbuilding.core._

case class event(robot: Int, task: Int) extends Command with Controllable {
  override def toString: String = s"e_${robot}_${task}"
}
case object shared extends Command with Controllable {
  override def toString: String = "s"
}