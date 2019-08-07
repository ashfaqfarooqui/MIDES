package modelbuilding.models.MachineBuffer

import modelbuilding.core._

import modelbuilding.core.modelInterfaces._

object MachineBuffer extends ModularModel {

  override val name: String = "MachineBufferMachine"
  override val modules: Set[String] = Set("Machine1", "Machine2")

  val alphabet = Alphabet(load1,load2,unload1,unload2)

  val stateString: String = "m1 m2"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[String,StateSet] = Map(
    "Machine1"->StateSet("m1"),
    "Machine2"->StateSet("m2")
  )

  override def eventMapping: Map[String,Alphabet] = Map(
    "Machine1" -> Alphabet(load1,unload1),
    "Machine2" -> Alphabet(load2,unload2)
  )

  val simulator = new SimulateMachineBuffer()

}
