package  modelbuilding.models.AGV

import modelbuilding.core.simulation.{SUL, Simulator}


class SulAgv extends SUL {

  override val simulator: Simulator = new SimulateAgv
  override val acceptsPartialStates = false

}