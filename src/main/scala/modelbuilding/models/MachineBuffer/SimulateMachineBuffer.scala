package modelbuilding.models.MachineBuffer

import modelbuilding.core._
import modelbuilding.core.interfaces.simulator.CodeSimulator

class SimulateMachineBuffer extends CodeSimulator {

  val m1 = "m1"
  val m2 = "m2"

  override val initState: StateMap               = StateMap(m1 -> false, m2 -> false)
  override val goalStates: Option[Set[StateMap]] = Some(Set(initState))

  val guardsC: Map[Command, Predicate] = Map(
    load1   -> EQ(m1, false),
    load2   -> EQ(m2, false),
    unload1 -> EQ(m1, true),
    unload2 -> EQ(m2, true)
  )

  val actionsC: Map[Command, List[Action]] = Map(
    load1   -> List(Toggle(m1)),
    load2   -> List(Toggle(m2)),
    unload1 -> List(Toggle(m1)),
    unload2 -> List(Toggle(m2))
  )
  override val guards: Map[String, Predicate] = guardsC.map(x => x._1.toString -> x._2)
  override val actions: Map[String, List[Action]] = actionsC.map(x => x._1.toString -> x._2)
}
