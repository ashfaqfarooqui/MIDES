package modelbuilding.models.MachineBuffer

import modelbuilding.core.{
  Action,
  AlwaysTrue,
  Assign,
  Command,
  EQ,
  Predicate,
  StateMap,
  Toggle,
  AND
}
import modelbuilding.core.simulators.OPCSimulator

class MBOPCSimulate extends OPCSimulator {

  /**
    * Define state we are interested in. These state variables will be synced with the actual system.
    */
  val m1 = "m1"
  val m2 = "m2"

  val run       = "run"
  val execState = "state"
  val string    = "string"

  override val stateExecVariable: String      = "GVL.S1"
  override val stateExecFinishedValue: String = "true"

  override val variableList = Some(
    List(
      ("GVL.R1", string),
      ("GVL.R2", string),
      ("GVL.R3", string),
      ("GVL.R4", string),
      ("GVL.Load_R1_initial", string),
      ("GVL.Load_R1_execute", string),
      ("GVL.Load_R1_finish", string),
      ("GVL.Unload_R1_initial", string),
      ("GVL.Unload_R1_execute", string),
      ("GVL.Unload_R1_finish", string),
      ("GVL.Load_R2_initial", string),
      ("GVL.Load_R2_execute", string),
      ("GVL.Load_R2_finish", string),
      ("GVL.Unload_R2_initial", string),
      ("GVL.Unload_R2_execute", string),
      ("GVL.Unload_R2_finish", string),
      ("GVL.RESET", string)
    )
  )

  override val goalStates: Option[Set[StateMap]] = None

  override val guards: Map[Command, Predicate] = Map(
    load1   -> EQ("GVL.Load_R1_initial", true), //make guard to be such that state is initial
    unload1 -> EQ("GVL.Unload_R1_initial", true),
    load2   -> EQ("GVL.Load_R2_initial", true),
    unload2 -> EQ("GVL.Unload_R2_initial", true)
  )

  override val actions: Map[Command, List[Action]] = Map(
    load1   -> List(Assign("GVL.R1", true)),
    unload1 -> List(Assign("GVL.R2", true)),
    load2   -> List(Assign("GVL.R3", true)),
    unload2 -> List(Assign("GVL.R4", true))
  )

  override val postGuards: Map[Command, Predicate] = Map(
    load1   -> EQ("GVL.Load_R1_finish", true), //make guard to be such that state is initial
    unload1 -> EQ("GVL.Unload_R1_finish", true),
    load2   -> EQ("GVL.Load_R2_finish", true),
    unload2 -> EQ("GVL.Unload_R2_finish", true)
  )

  //Remember: this can be if the action succeds or fails
  override val postActions: Map[Command, List[Action]] = Map(
    load1   -> List(Assign("GVL.R1", false)),
    unload1 -> List(Assign("GVL.R2", false)),
    load2   -> List(Assign("GVL.R3", false)),
    unload2 -> List(Assign("GVL.R4", false))
  )

}
