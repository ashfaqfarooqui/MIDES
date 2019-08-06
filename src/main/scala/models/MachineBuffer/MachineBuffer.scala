package models.MachineBuffer

import core._

import core.modelInterfaces._

object MachineBuffer extends model {


  val l1 = Symbol(load1)
  val l2 = Symbol(load2)
  val u1 = Symbol(unload1)
  val u2 = Symbol(unload2)

  val t = Symbol(tou)
  override val A: Alphabets = Alphabets(Set(l1,l2,u1,u2,t))

  val eventMapping:Map[String,Set[Symbol]] = Map(
    "Machine2"->Set(t,l2,u2),
    "Machine1"->Set(t,l1,u1)
  )


  override val name: String = "MachineBufferMachine"
}
