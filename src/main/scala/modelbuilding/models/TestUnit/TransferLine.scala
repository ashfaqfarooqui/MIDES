package modelbuilding.models.TestUnit

import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core.{Alphabet, _}

object TransferLine extends ModularModel {

  override val name: String = "TransferLine"
  override val alphabet     = Alphabet(start1, start2, finish1, finish2, accept, reject, test)

  override val modules: Set[String] = Set.empty

  val stateString: String       = "m1 m2 tu"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[String, StateSet] = Map(
    "M1" -> StateSet("m1"),
    "M2" -> StateSet("m2"),
    "TU" -> StateSet("tu")
  )
  override def eventMapping: Map[String, Alphabet] = Map(
    "TU" -> Alphabet(accept, test, reject),
    "M1" -> Alphabet(finish1, start1),
    "M2" -> Alphabet(finish2, start2)
  )

}
