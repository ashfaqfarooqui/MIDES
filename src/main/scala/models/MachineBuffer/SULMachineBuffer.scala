package models.MachineBuffer

import core.modelInterfaces._
class SULMachineBuffer extends SUL{
    override val simulator: Simulator = new SimulateMachineBuffer

}
