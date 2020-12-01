package CatAndMouse

import modelbuilding.core._
import modelbuilding.core.interfaces.modeling.MonolithicModel
object CatAndMouse extends MonolithicModel {

  override val name: String = "CatAndMouse"
  override val alphabet     = Alphabet(c1, c2, c3, c4, c5, c6, c7, m1, m2, m3, m4, m5, m6)
  override val states       = StateSet("r0", "r1", "r2", "r3", "r4")
}
