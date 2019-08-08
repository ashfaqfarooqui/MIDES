package modelbuilding.models.MachineBuffer

<<<<<<< HEAD
import modelbuilding.core.simulation.{SUL, Simulator}
=======
import modelbuilding.core.StateMap
import modelbuilding.core.modelInterfaces._
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.

class SULMachineBuffer extends SUL {

  override val simulator: Simulator = new SimulateMachineBuffer
<<<<<<< HEAD
  override val acceptsPartialStates = false
=======

  override val stateToString: StateMap => Option[String] =
    (s: StateMap) => Some(s.state.map(v => s"(${v._1}=${v._2})").mkString(","))
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.

}
