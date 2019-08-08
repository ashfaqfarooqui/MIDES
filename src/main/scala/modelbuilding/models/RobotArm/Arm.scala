package modelbuilding.models.RobotArm

import modelbuilding.core._
import modelbuilding.core.modelInterfaces._

object Arm extends MonolithicModel {

  override val name: String = "RoboticArm"
  //override val alphabet = Alphabet(up,down,left,right,extend,retract,grip,release)
  override val alphabet = Alphabet(up,down,left,right)

  override val simulation = new SULArm(5,5)

}
