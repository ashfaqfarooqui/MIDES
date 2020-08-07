package modelbuilding.models.MachineBuffer

import modelbuilding.core._
import modelbuilding.core.interfaces.simulator.CodeSimulator

class SimulateMachineBufferWithControl extends CodeSimulator {

  val m1 = "m1"
  val m2 = "m2"
  val b  = "b"

  override val initState: StateMap               = StateMap(m1 -> false, m2 -> false, b -> false)
  override val goalStates: Option[Set[StateMap]] = Some(Set(initState))

  val guardsC: Map[Command, Predicate] = Map(
    load1   -> EQ(m1, false),
    unload1 -> AND(EQ(m1, true), EQ(b, false)),
    load2   -> AND(EQ(m2, false), EQ(b, true)),
    unload2 -> EQ(m2, true)
  )

  val actionsC: Map[Command, List[Action]] = Map(
    load1   -> List(Toggle(m1)),
    load2   -> List(Toggle(m2), Toggle(b)),
    unload1 -> List(Toggle(m1), Toggle(b)),
    unload2 -> List(Toggle(m2))
  )

  override val guards: Map[String, Predicate] = guardsC.map(x => x._1.toString -> x._2)
  override val actions: Map[String, List[Action]] = actionsC.map(x => x._1.toString -> x._2)
}
