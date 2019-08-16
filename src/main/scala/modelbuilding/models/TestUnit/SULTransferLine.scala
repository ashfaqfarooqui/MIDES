package modelbuilding.models.TestUnit

import modelbuilding.core.simulation.{SUL, Simulator}
class SULTransferLine extends SUL {
  override val simulator: Simulator = new SimulateTL
  override val acceptsPartialStates = true
}
