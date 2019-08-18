package modelbuilding.models.TestUnit

import modelbuilding.core._
import modelbuilding.core.modeling.{ModularModel, Specifications}


object TransferLine extends ModularModel with Specifications {

  override val name: String = "TransferLine"
  override val alphabet = Alphabet(true,start1,start2,finish1,finish2,accept,reject,test)
  override val simulation = new SULTransferLine()

  override val modules: Set[String] = Set.empty

  val stateString: String = "m1 m2 tu"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[String,StateSet] = Map(
    "M1"->StateSet("m1"),
    "M2"->StateSet("m2"),
    "TU"->StateSet("tu")
  )
  override def eventMapping: Map[String, Alphabet] = Map(
    "TU"->Alphabet(accept,test,reject),
    "M1"->Alphabet(finish1,start1),
    "M2"->Alphabet(finish2,start2)
  )

  override val specFilePath: Option[String] = Some("SupremicaModels/TransferLineFull.wmod")
  addSpecsFromSupremica(specFilePath.get)
}
