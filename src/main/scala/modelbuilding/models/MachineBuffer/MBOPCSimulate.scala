package modelbuilding.models.MachineBuffer

import modelbuilding.core.{Action, AlwaysTrue, Assign, Command, EQ, Predicate, StateMap, Toggle}
import modelbuilding.core.simulation.OPCSimulator

class MBOPCSimulate extends OPCSimulator {


  /**
   * Define state we are interested in. These state variables will be synced with the actual system.
   */
  val m1 = "m1"
  val m2 = "m2"


  override val goalStates: Option[Set[StateMap]] = None


  override val guards: Map[Command, Predicate] = Map(
    load1 -> AlwaysTrue,
    load2 -> AlwaysTrue,
    unload1 -> AlwaysTrue,
    unload2 -> AlwaysTrue
  )

  override val actions: Map[Command, List[Action]] = Map(
    load1 -> List(Assign(m1, true)),
    load2 -> List(Assign(m2,true)),
    unload1 -> List(Assign(m1,false)),
    unload2 -> List(Assign(m2,false))
  )
}
