package modelbuilding.models.CatAndMouseModular

import modelbuilding.core._
import modelbuilding.core.modeling._

object CatAndMouseModular extends ModularModel with Specifications {

  override val name: String = "CatAndMouseModular"
  override val alphabet = Alphabet(c1,c2,c3,c4,c5,c6,c7,m1,m2,m3,m4,m5,m6)
  override val simulation = new SULCatAndMouseModular()

  override val modules: Set[String] = Set("Cat", "Mouse")

  val stateString: String = "cat mouse"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[String,StateSet] = Map(
    "Cat"->StateSet("cat"),
    "Mouse"->StateSet("mouse")
  )

  override def eventMapping: Map[String,Alphabet] = Map(
    "Cat" -> Alphabet(c1,c2,c3,c4,c5,c6,c7),
    "Mouse" -> Alphabet(m1,m2,m3,m4,m5,m6)
  )

  override val specFilePath: Option[String] = Some("SupremicaModels/CatAndMouse.wmod")
  addSpecsFromSupremica(specFilePath.get)

}
