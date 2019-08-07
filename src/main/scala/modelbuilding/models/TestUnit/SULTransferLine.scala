package modelbuilding.models.TestUnit

import modelbuilding.core.modelInterfaces.{SUL, Simulator}
class SULTransferLine extends SUL {
  override val simulator: Simulator = new SimulateTL
}
