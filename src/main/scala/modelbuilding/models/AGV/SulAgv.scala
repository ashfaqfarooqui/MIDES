package  modelbuilding.models.AGV

import modelbuilding.core.{Action, Command, StateMap}
import modelbuilding.core.modelInterfaces.{SUL, Simulator}


class SulAgv extends SUL {

  override val simulator: Simulator = new SimulateAgv
  override val acceptsPartialStates = true

}