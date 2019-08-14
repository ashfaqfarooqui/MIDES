package modelbuilding.models.RobotArm

import modelbuilding.core.modelInterfaces._

class SULArm(x:Int,y:Int) extends SUL {
  override val simulator: Simulator = new SimulateArm(x,y)
  override val acceptsPartialStates = false
}
