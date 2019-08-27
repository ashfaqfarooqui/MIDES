package modelbuilding.models.CatAndMouse

import modelbuilding.core.simulation.{SUL, Simulator}

class SULCatAndMouse extends SUL {
  override val simulator: Simulator = new SimulateCatAndMouse
}
