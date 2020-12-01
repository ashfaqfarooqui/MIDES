package RobotArm

import modelbuilding.core._
import modelbuilding.core.interfaces.modeling.MonolithicModel

object Arm extends MonolithicModel {

  override val name: String = "RoboticArm"
  //override val alphabet = Alphabet(up,down,left,right,extend,retract,grip,release)
  override val alphabet = Alphabet(up, down, left, right)
  override val states   = StateSet("x", "y", "extended", "gripped")
}
