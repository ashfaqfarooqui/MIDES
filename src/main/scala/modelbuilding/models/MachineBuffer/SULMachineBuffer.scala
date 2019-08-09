package modelbuilding.models.MachineBuffer

import modelbuilding.core.modelInterfaces._

class SULMachineBuffer extends SUL {

  override val simulator: Simulator = new SimulateMachineBuffer

}
