package modelbuilding.models.CatAndMouseModular

import modelbuilding.core._
<<<<<<< HEAD
import modelbuilding.core.modeling._

object CatAndMouseModular extends ModularModel with Specifications {
=======
import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.core.modelInterfaces._

object CatAndMouseModular extends ModularModel {
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.

  override val name: String = "CatAndMouseModular"
  override val alphabet = Alphabet(c1,c2,c3,c4,c5,c6,c7,m1,m2,m3,m4,m5,m6)
  override val simulation = new SULCatAndMouseModular()

<<<<<<< HEAD
  override val modules: Set[String] = Set("Cat", "Mouse")
=======
  override val modules: Set[Module] = Set("Cat", "Mouse")
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.

  val stateString: String = "cat mouse"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

<<<<<<< HEAD
  override def stateMapping: Map[String,StateSet] = Map(
=======
  override def stateMapping: Map[Module,StateSet] = Map(
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.
    "Cat"->StateSet("cat"),
    "Mouse"->StateSet("mouse")
  )

<<<<<<< HEAD
  override def eventMapping: Map[String,Alphabet] = Map(
=======
  override def eventMapping: Map[Module,Alphabet] = Map(
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.
    "Cat" -> Alphabet(c1,c2,c3,c4,c5,c6,c7),
    "Mouse" -> Alphabet(m1,m2,m3,m4,m5,m6)
  )

<<<<<<< HEAD
  override val specFilePath: Option[String] = Some("SupremicaModels/CatAndMouse.wmod")
  addSpecsFromSupremica(specFilePath.get)

=======
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.
}
