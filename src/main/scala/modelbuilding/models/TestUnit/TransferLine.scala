package modelbuilding.models.TestUnit

import modelbuilding.core.modelInterfaces._
import modelbuilding.core._


object TransferLine extends MonolithicModel {

  override val name: String = "TransferLine"
  override val alphabet = Alphabet(start1,start2,finish1,finish2,accept,reject,test)
  override val simulation = new SULTransferLine()

  /*val eventMapping:Map[String,Alphabet] = Map(
    "TU"->Alphabet(accept,test,reject),
    "M1"->Alphabet(finish1,start1),
    "M2"->Alphabet(finish2,start2)
  )*/

}
