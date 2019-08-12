package modelbuilding.models.StickPicking

import modelbuilding.core.modelInterfaces.{SUL, Simulator}

class SULSticks(n:Int) extends SUL{
  override val simulator: Simulator = new SimulateSticks(n)
  override val acceptsPartialStates = false
}
