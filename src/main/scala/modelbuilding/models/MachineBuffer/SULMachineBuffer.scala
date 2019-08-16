package modelbuilding.models.MachineBuffer

import modelbuilding.core.simulation.{SUL, Simulator}

class SULMachineBuffer extends SUL {

  override val simulator: Simulator = new SimulateMachineBuffer
  override val acceptsPartialStates = false

}
