package modelbuilding.models.MachineBuffer

import modelbuilding.core._
import modelbuilding.core.interfaces.simulator.CodeSimulator

class SimulateMachineBufferWithControl extends CodeSimulator {

  val m1 = "m1"
  val m2 = "m2"
  val b  = "b"

  override val initState: StateMap               = StateMap(m1 -> false, m2 -> false, b -> false)
  override val goalStates: Option[Set[StateMap]] = Some(Set(initState))

  override val guards: Map[Command, Predicate] = Map(
    load1   -> EQ(m1, false),
    unload1 -> AND(EQ(m1, true), EQ(b, false)),
    load2   -> AND(EQ(m2, false), EQ(b, true)),
    unload2 -> EQ(m2, true)
  )

  override val actions: Map[Command, List[Action]] = Map(
    load1   -> List(Toggle(m1)),
    load2   -> List(Toggle(m2), Toggle(b)),
    unload1 -> List(Toggle(m1), Toggle(b)),
    unload2 -> List(Toggle(m2))
  )
}
