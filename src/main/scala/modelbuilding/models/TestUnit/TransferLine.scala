package modelbuilding.models.TestUnit

import modelbuilding.core.modelInterfaces._
import modelbuilding.core._


object TransferLine extends Model {


  val s1 = Symbol(start1)
  val f1 = Symbol(finish1)
  val s2 = Symbol(start2)
  val f2 = Symbol(finish2)
  val I = Symbol(test)
  val a = Symbol(accept)
  val r = Symbol(reject)


  val t = Symbol(tou)



  override val A = Alphabets(Set(t,s1,s2,f1,f2,a,r,I))
  val eventMapping:Map[String,Set[Symbol]] = Map(
    "TU"->Set(t,a,I,r),
    "M1"->Set(t,f1,s1),
    "M2"->Set(t,f2,s2)
  )



  override val name: String = "TransferLine"
  override val simulation: SUL = new SULTransferLine
}
