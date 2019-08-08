package modelbuilding.models.CatAndMouseModular

import modelbuilding.core.StateMap
import modelbuilding.core.modelInterfaces.{SUL, Simulator}

class SULCatAndMouseModular extends SUL{

  override val simulator: Simulator = new SimulateCatAndMouseModular

  override val stateToString: StateMap => Option[String] =
    (s: StateMap) => Some(s.state.map(v => s"(${v._1}=${v._2})").mkString(","))

}
