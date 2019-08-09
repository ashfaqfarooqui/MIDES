package modelbuilding.models.CatAndMouseModular

<<<<<<< HEAD
import modelbuilding.core.simulation.{SUL, Simulator}
import modelbuilding.core.StateMap
=======
import modelbuilding.core.modelInterfaces.{SUL, Simulator}
>>>>>>> 1. Updated the Simulation structure

class SULCatAndMouseModular extends SUL{

  override val simulator: Simulator = new SimulateCatAndMouseModular
<<<<<<< HEAD
  override val acceptsPartialStates = false
=======
>>>>>>> 1. Updated the Simulation structure

}
