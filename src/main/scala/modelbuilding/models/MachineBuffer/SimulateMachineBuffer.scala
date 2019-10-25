package modelbuilding.models.MachineBuffer

import modelbuilding.core._
import modelbuilding.core.simulators.{CodeSimulator, Simulator}

class SimulateMachineBuffer extends CodeSimulator {

  val m1 = "m1"
  val m2 = "m2"

  override val initState: StateMap               = StateMap(m1 -> false, m2 -> false)
  override val goalStates: Option[Set[StateMap]] = Some(Set(initState))

  override val guards: Map[Command, Predicate] = Map(
    load1   -> EQ(m1, false),
    load2   -> EQ(m2, false),
    unload1 -> EQ(m1, true),
    unload2 -> EQ(m2, true)
  )

  override val actions: Map[Command, List[Action]] = Map(
    load1   -> List(Toggle(m1)),
    load2   -> List(Toggle(m2)),
    unload1 -> List(Toggle(m1)),
    unload2 -> List(Toggle(m2))
  )
}
