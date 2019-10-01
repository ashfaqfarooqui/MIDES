package modelbuilding.models.MachineBuffer

import modelbuilding.core.{Action, AlwaysTrue, Assign, Command, EQ, Predicate, StateMap, Toggle}
import modelbuilding.core.simulation.OPCSimulator

class MBOPCSimulate extends OPCSimulator {

  override val variableList= Some(List(run,execState))

  /**
   * Define state we are interested in. These state variables will be synced with the actual system.
   */
  val m1 = "m1"
  val m2 = "m2"

  val run = "run"
  val execState = "state"

  override val goalStates: Option[Set[StateMap]] = None


  override val guards: Map[Command, Predicate] = Map(
    load1 -> EQ(execState,"init"), //make guard to be such that state is initial
    load2 -> EQ(execState,"init"),
    unload1 -> EQ(execState,"init"),
    unload2 -> EQ(execState,"init")
  )

  override val actions: Map[Command, List[Action]] = Map(
    load1 -> List(Assign(run, "load1")),
    load2 -> List(Assign(run,"load2")),
    unload1 -> List(Assign(run,"unload1")),
    unload2 -> List(Assign(run,"unload2"))
  )
}
