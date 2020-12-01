package ZenuityLaneChange

import ZenuityLaneChange.Simulator.ZenuitySimulator
import modelbuilding.core._

class LaneChangeSimulate extends ZenuitySimulator {

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
  override val goalPredicate: Option[AND] = Some(
    AND(
      List(
        EQ(state, "stateA"),
        EQ(direction, "none"),
        EQ(laneChangeRequest, "none"),
        EQ(b1, false),
        EQ(b2, false)
      )
    )
  )
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
  val initialInternalState: StateMap = getInitialState
  //println(client.getEngine.feval(3, "gcd", Int.box(40), Int.box(60)))
  override val initState: StateMap =
    StateMap(
      // laneChgRequest -> CANCEL,
      // decVar         -> initialDecisionMap,
      // internalState  -> intialInternalState
      initialSelfState + (laneChngReq -> CANCEL) ++ initialDecisionMap
    )

  override val goalStates: Option[Set[StateMap]] = None
  override val guards: Map[Command, Predicate] = Map(
    goRight       -> AlwaysTrue,
    goLeft        -> AlwaysTrue,
    cancelRequest -> AlwaysTrue,
    b11true       -> AlwaysTrue,
    b12true       -> AlwaysTrue,
    b3true        -> AlwaysTrue,
    b4true        -> AlwaysTrue,
    b5true        -> AlwaysTrue,
    b6true        -> AlwaysTrue,
    b7true        -> AlwaysTrue,
    b8true        -> AlwaysTrue,
    b9true        -> AlwaysTrue,
    b10true       -> AlwaysTrue,
    b11false      -> AlwaysTrue,
    b12false      -> AlwaysTrue,
    b3false       -> AlwaysTrue,
    b4false       -> AlwaysTrue,
    b5false       -> AlwaysTrue,
    b6false       -> AlwaysTrue,
    b7false       -> AlwaysTrue,
    b8false       -> AlwaysTrue,
    b9false       -> AlwaysTrue,
    b10false      -> AlwaysTrue
  )
  override val actions: Map[Command, List[Action]] = Map(
    goRight       -> List(Assign(laneChngReq, RIGHT)),
    goLeft        -> List(Assign(laneChngReq, LEFT)),
    cancelRequest -> List(Assign(laneChngReq, CANCEL)),
    b11true       -> List(Assign(b11, true)),
    b12true       -> List(Assign(b12, true)),
    b3true        -> List(Assign(b3, true)),
    b4true        -> List(Assign(b4, true)),
    b5true        -> List(Assign(b5, true)),
    b6true        -> List(Assign(b6, true)),
    b7true        -> List(Assign(b7, true)),
    b8true        -> List(Assign(b8, true)),
    b9true        -> List(Assign(b9, true)),
    b10true       -> List(Assign(b10, true)),
    b11false      -> List(Assign(b11, false)),
    b12false      -> List(Assign(b12, false)),
    b3false       -> List(Assign(b3, false)),
    b4false       -> List(Assign(b4, false)),
    b5false       -> List(Assign(b5, false)),
    b6false       -> List(Assign(b6, false)),
    b7false       -> List(Assign(b7, false)),
    b8false       -> List(Assign(b8, false)),
    b9false       -> List(Assign(b9, false)),
    b10false      -> List(Assign(b10, false))
  )
}

object LaneChangeSimulate {
  def apply(): LaneChangeSimulate = new LaneChangeSimulate()
}
