package modelbuilding.models.CatAndMouse

import modelbuilding.core.modelInterfaces._
import modelbuilding.core._
object CatAndMouse extends Model{
  val c1s = Symbol(c1)
  val c2s = Symbol(c2)
  val c3s = Symbol(c3)
  val c4s = Symbol(c4)
  val c5s = Symbol(c5)
  val c6s = Symbol(c6)
  val c7s = Symbol(c7)
  val m1s = Symbol(m1)
  val m2s = Symbol(m2)
  val m3s = Symbol(m3)
  val m4s = Symbol(m4)
  val m5s = Symbol(m5)
  val m6s = Symbol(m6)

  val t = Symbol(tou)



  override val A = Alphabets(Set(t,c1s,c2s,c3s,c4s,c5s,c6s,c7s,m1s,m2s,m3s,m4s,m5s,m6s))


  override val name: String = "CatAndMouse"
}
