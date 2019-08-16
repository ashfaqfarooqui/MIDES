package modelbuilding.models.RobotArm

import modelbuilding.core._
import modelbuilding.core.modeling.MonolithicModel

object Arm extends MonolithicModel {

  override val name: String = "RoboticArm"
  //override val alphabet = Alphabet(up,down,left,right,extend,retract,grip,release)
  override val alphabet = Alphabet(up,down,left,right)
  override val states = StateSet("x","y","extended","gripped")

  override val simulation = new SULArm(5,5)

}
