package modelbuilding.models.MachineBuffer

import modelbuilding.core.StateMap
import modelbuilding.core.modelInterfaces._

class SULMachineBuffer extends SUL {

  override val simulator: Simulator = new SimulateMachineBuffer

  override val stateToString: StateMap => Option[String] =
    (s: StateMap) => Some(s.state.map(v => s"(${v._1}=${v._2})").mkString(","))

}
