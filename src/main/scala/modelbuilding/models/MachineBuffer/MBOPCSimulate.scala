package modelbuilding.models.MachineBuffer

import modelbuilding.core.{Action, AlwaysTrue, Assign, Command, EQ, Predicate, StateMap, Toggle}
import modelbuilding.core.simulators.OPCSimulator

class MBOPCSimulate extends OPCSimulator {

  /**
   * Define state we are interested in. These state variables will be synced with the actual system.
   */
  val m1 = "m1"
  val m2 = "m2"

  val run = "run"
  val execState = "state"
  val string = "string"

  override val stateExecVariable: String = _
  override val stateExecFinishedValue: String = _
  override val stateExecInitialValue: String = _

  override val variableList= Some(List((run,string),(execState,string)))


  override val goalStates: Option[Set[StateMap]] = None


  override val guards: Map[Command, Predicate] = Map(
    load1 -> EQ(stateExecVariable,stateExecInitialValue), //make guard to be such that state is initial
    load2 -> EQ(stateExecVariable,stateExecInitialValue),
    unload1 -> EQ(stateExecVariable,stateExecInitialValue),
    unload2 -> EQ(stateExecVariable,stateExecInitialValue)
  )

  override val actions: Map[Command, List[Action]] = Map(
    load1 -> List(Assign(run, true)),
    load2 -> List(Assign(run,true)),
    unload1 -> List(Assign(run,true)),
    unload2 -> List(Assign(run,true))
  )

  override val postGuards: Map[Command, Predicate] = Map(
    load1 -> EQ(stateExecVariable,stateExecFinishedValue), //make guard to be such that state is initial
    load2 -> EQ(stateExecVariable,stateExecFinishedValue),
    unload1 -> EQ(stateExecVariable,stateExecFinishedValue),
    unload2 -> EQ(stateExecVariable,stateExecFinishedValue)
  )

  override val postActions: Map[Command, List[Action]] = Map(
    load1 -> List(Assign(run, false)),
    load2 -> List(Assign(run,false)),
    unload1 -> List(Assign(run,false)),
    unload2 -> List(Assign(run,false))
  )

}
