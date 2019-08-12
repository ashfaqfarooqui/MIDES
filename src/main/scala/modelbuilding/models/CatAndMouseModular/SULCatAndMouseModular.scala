package modelbuilding.models.CatAndMouseModular

import modelbuilding.core.simulation.{SUL, Simulator}

class SULCatAndMouseModular extends SUL{

  override val simulator: Simulator = new SimulateCatAndMouseModular
  override val acceptsPartialStates = false

}
