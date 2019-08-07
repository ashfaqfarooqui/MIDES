package modelbuilding.models.CatAndMouse

import modelbuilding.core.modelInterfaces._
import modelbuilding.core._
object CatAndMouse extends MonolithicModel {

  override val name: String = "CatAndMouse"
  override val alphabet = Alphabet(c1,c2,c3,c4,c5,c6,c7,m1,m2,m3,m4,m5,m6)
  override val simulation = new SULCatAndMouse()



  override val simulation = new SULCatAndMouse

}
