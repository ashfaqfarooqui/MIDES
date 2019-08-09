package  modelbuilding.models.AGV

import modelbuilding.core.simulation.{SUL, Simulator}
import modelbuilding.core.{Action, Command, StateMap}

class SulAgv extends SUL {

  override val simulator: Simulator = new SimulateAgv
  override val acceptsPartialStates = false

}