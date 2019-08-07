package modelbuilding.models.MachineBuffer

import modelbuilding.core._

import modelbuilding.core.modelInterfaces._

object MachineBuffer extends Model {


  val l1 = Symbol(load1)
  val l2 = Symbol(load2)
  val u1 = Symbol(unload1)
  val u2 = Symbol(unload2)

  val t = Symbol(tou)
  override val A: Alphabets = Alphabets(Set(l1,l2,u1,u2,t))
  override val simulation = new SULMachineBuffer
  val eventMapping:Map[String,Set[Symbol]] = Map(
    "Machine2"->Set(t,l2,u2),
    "Machine1"->Set(t,l1,u1)
  )


  override val name: String = "MachineBufferMachine"

  val sim = new SULMachineBuffer()
}
