package main.scala.models.StickPicking

import core._
import core.modelInterfaces._



object Sticks extends model {
  val p1rem1 = Symbol(e11)
  val p2rem1 = Symbol(e21)
  val p1rem2 = Symbol(e12)
  val p2rem2 = Symbol(e22)
  val p1rem3 = Symbol(e13)
  val p2rem3 = Symbol(e23)

  val t = Symbol(tou)



  override val A = Alphabets(Set(t,p1rem1,p1rem2,p2rem1,p2rem2))


  override val name: String = "Sticks"
}
