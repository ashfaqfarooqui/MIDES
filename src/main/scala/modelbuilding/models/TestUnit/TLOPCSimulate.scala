package modelbuilding.models.TestUnit

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

class TLOPCSimulate extends OPCSimulator {

  /**
    * Define state we are interested in. These state variables will be synced with the actual system.
    */
  val m1 = "m1"
  val m2 = "m2"
  val tu = "tu"

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
      ("GVL.R5", string),
      ("GVL.R6", string),
      ("GVL.R7", string),
      ("GVL.S1", string),
      ("GVL.S2", string),
      ("GVL.S3", string),
      ("GVL.S4", string),
      ("GVL.S5", string),
      ("GVL.S6", string),
      ("GVL.S7", string),
      ("GVL.S8", string),
      ("GVL.RESET", string)
    )
  )

  override val goalStates: Option[Set[StateMap]] = None

  override val guards: Map[Command, Predicate] = Map(
    start1  -> AND(EQ("GVL.S1", true), EQ("GVL.S2", false)),
    finish1 -> EQ("GVL.S2", true),
    start2  -> AND(EQ("GVL.S3", true), EQ("GVL.S4", false)),
    finish2 -> EQ("GVL.S4", true),
    test    -> AND(EQ("GVL.S5", true), EQ("GVL.S6", false)),
    accept  -> EQ("GVL.S6", true),
    reject  -> EQ("GVL.S6", true)
  )

  override val actions: Map[Command, List[Action]] = Map(
    start1  -> List(Assign("GVL.R1", true)),
    finish1 -> List(Assign("GVL.R2", true)),
    start2  -> List(Assign("GVL.R3", true)),
    finish2 -> List(Assign("GVL.R4", true)),
    test    -> List(Assign("GVL.R5", true)),
    accept  -> List(Assign("GVL.R6", true)),
    reject  -> List(Assign("GVL.R7", true))
  )

  override val postGuards: Map[Command, Predicate] = Map(
    start1  -> EQ("GVL.S1", false),
    finish1 -> EQ("GVL.S2", false),
    start2  -> EQ("GVL.S3", false),
    finish2 -> EQ("GVL.S4", false),
    test    -> EQ("GVL.S5", false),
    accept  -> EQ("GVL.S6", false),
    reject  -> EQ("GVL.S6", false)
  )

  //Remember: this can be if the action success or fails
  override val postActions: Map[Command, List[Action]] = Map(
    start1  -> List(Assign("GVL.R1", false)),
    finish1 -> List(Assign("GVL.R2", false)),
    start2  -> List(Assign("GVL.R3", false)),
    finish2 -> List(Assign("GVL.R4", false)),
    test    -> List(Assign("GVL.R5", false)),
    accept  -> List(Assign("GVL.R6", false)),
    reject  -> List(Assign("GVL.R7", false))
  )

}
