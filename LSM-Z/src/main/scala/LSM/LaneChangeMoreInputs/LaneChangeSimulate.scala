package LSM.LaneChangeMoreInputs

import LSM.MatlabSimulator.MatlabSimulator
import modelbuilding.core._

class LaneChangeSimulateMonolithic extends MatlabSimulator {

  val internalState = "self"
  val decVar        = "decision_var"
  // val laneChngReq   = "laneChngReq"
  //
  // val state             = "state"
  // val direction         = "direction"
  // val laneChangeRequest = "laneChangeRequest"
  // val b1                = "b1"
  //val b2                = "b2"
  val initialSelfState = Map(
    state             -> "stateA",
    direction         -> "none",
    laneChangeRequest -> false,
    b1                -> false,
    b2                -> false
  )
  override val goalPredicate = Some(AlwaysTrue)
  /*
  Some(
    AND(
      List(
        EQ(state, "stateA"),
        EQ(direction, "none"),
        EQ(laneChangeRequest, false),
        EQ(b1, false),
        EQ(b2, false)
      )
    )
 )*/

  // val b1  = "b1"
  // val b2  = "b2"
  // val b3  = "b3"
  // val b4  = "b4"
  // val b5  = "b5"
  // val b6  = "b6"
  // val b7  = "b7"
  // val b8  = "b8"
  // val b9  = "b9"
  // val b10 = "b10"
  // val b11 = "b11"
  // val b12 = "b12"

  val initialDecisionMap =
    Map(
      // b1  -> false,
      // b2  -> false,
      b3  -> false,
      b4  -> false,
      b5  -> false,
      b6  -> false,
      b7  -> false,
      b8  -> false,
      b9  -> false,
      b10 -> false,
      b11 -> false,
      b12 -> false
    )

  /** if this is queried anytime after the execution has begun the value will be wrong.
    * instead look for a way to reset matlab.
    */
  val initialInternalState = getInitialState
  //println(client.getEngine.feval(3, "gcd", Int.box(40), Int.box(60)))
  override val initState: StateMap =
    StateMap(
      // laneChgRequest -> CANCEL,
      // decVar         -> initialDecisionMap,
      // internalState  -> intialInternalState
      initialSelfState + (laneChngReq -> "none") ++ initialDecisionMap
    )

  override val goalStates: Option[Set[StateMap]]   = None
  override val guards: Map[Command, Predicate]     = LaneChange.ops._2
  override val actions: Map[Command, List[Action]] = LaneChange.ops._3
}

object LaneChangeSimulate {
  def apply(): LaneChangeSimulateMonolithic = new LaneChangeSimulateMonolithic()
}
