package modelbuilding.models.RobotArm

import modelbuilding.core._
import modelbuilding.core.modelInterfaces._

object arm extends Model {
  val g = Symbol(grip)
  val ret = Symbol(retract)
  val e = Symbol(extend)
  val rel = Symbol(release)
  val u = Symbol(up)
  val d = Symbol(down)
  val l = Symbol(left)
  val r = Symbol(right)
  val t = Symbol(tou)



  override val A = Alphabets(Set(t,u,d,l,r,e,ret,g,rel))


  override val name: String = "RoboticArm"
}
