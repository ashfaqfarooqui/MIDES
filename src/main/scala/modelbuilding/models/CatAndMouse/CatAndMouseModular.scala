package modelbuilding.models.CatAndMouse

import modelbuilding.core._
import modelbuilding.core.modeling.ModularModel

object CatAndMouseModular extends ModularModel {

  override val name: String = "CatAndMouseModular"
  override val alphabet = Alphabet(c1,c2,c3,c4,c5,c6,c7,m1,m2,m3,m4,m5,m6)

  override val modules: Set[String] = Set("Cat", "Mouse")

  val stateString: String = "r0 r1 r2 r3 r4 r5"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[String,StateSet] = Map(
    "Cat"->states,
    "Mouse"->states
  )

  override def eventMapping: Map[String,Alphabet] = Map(
    "Cat" -> Alphabet(c1,c2,c3,c4,c5,c6,c7),
    "Mouse" -> Alphabet(m1,m2,m3,m4,m5,m6)
  )

}
