package modelbuilding.models.TestUnit

import modelbuilding.core.modelInterfaces._
import modelbuilding.core._


object TransferLine extends ModularModel {

  override val name: String = "TransferLine"
  override val alphabet = Alphabet(start1,start2,finish1,finish2,accept,reject,test)
  override val simulation = new SULTransferLine()
  override val specFilePath: Option[String] = Some("SupremicaModels/TransferLineFull.wmod")

  /*val eventMapping:Map[String,Alphabet] = Map(
    "TU"->Alphabet(accept,test,reject),
    "M1"->Alphabet(finish1,start1),
    "M2"->Alphabet(finish2,start2)
  )*/
  override val modules: Set[ModularModel.Module] = Set.empty

  val stateString: String = "m1 m2 tu"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[ModularModel.Module,StateSet] = Map(
    "M1"->StateSet("m1"),
    "M2"->StateSet("m2"),
    "TU"->StateSet("tu")
  )
  override def eventMapping: Map[ModularModel.Module, Alphabet] = Map(
    "TU"->Alphabet(accept,test,reject),
    "M1"->Alphabet(finish1,start1),
    "M2"->Alphabet(finish2,start2)
  )
}
