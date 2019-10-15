package modelbuilding.models.ZenuityLaneChange

import modelbuilding.core
import modelbuilding.core.externalClients.ZenuityClient
import modelbuilding.core.{
  Action,
  AlwaysTrue,
  Assign,
  AssignInMap,
  Command,
  Predicate,
  StateMap
}
import modelbuilding.core.simulators.ZenuitySimulator

import scala.collection.JavaConverters._
class LaneChangeSimulate extends ZenuitySimulator {

  val client = ZenuityClient()
  // def getState = client.getEngine.feval(3,"gcd", Int.box(40),  Int.box(60))

  val internalState  = "internalState"
  val decVar         = "decVar"
  val laneChgRequest = "laneChgRequest"
  val b1             = "b1"
  val b2             = "b2"
  val b3             = "b3"
  val b4             = "b4"
  val b5             = "b5"
  val b6             = "b6"
  val b7             = "b7"

  val initialDecisionMap =
    Map(
      b1 -> false,
      b2 -> false,
      b3 -> false,
      b4 -> false,
      b5 -> false,
      b6 -> false,
      b7 -> false
    )

  /**
    * if this is queried anytime after the execution has begun the value will be wrong.
    * instead look for a way to reset matlab.
    */
  // val intialInternalState = getState
  println(client.getEngine.feval(3, "gcd", Int.box(40), Int.box(60)))
  override val initState: StateMap =
    StateMap(laneChgRequest -> CANCEL, decVar -> initialDecisionMap)
  override val goalStates: Option[Set[StateMap]] = None
  override val guards: Map[Command, Predicate] = Map(
    goRight       -> AlwaysTrue,
    goLeft        -> AlwaysTrue,
    cancelRequest -> AlwaysTrue,
    b1true        -> AlwaysTrue,
    b2true        -> AlwaysTrue,
    b3true        -> AlwaysTrue,
    b4true        -> AlwaysTrue,
    b5true        -> AlwaysTrue,
    b6true        -> AlwaysTrue,
    b7true        -> AlwaysTrue,
    b1false       -> AlwaysTrue,
    b2false       -> AlwaysTrue,
    b3false       -> AlwaysTrue,
    b4false       -> AlwaysTrue,
    b5false       -> AlwaysTrue,
    b6false       -> AlwaysTrue,
    b7false       -> AlwaysTrue
  )
  override val actions: Map[Command, List[Action]] = Map(
    goRight       -> List(Assign(laneChgRequest, RIGHT)),
    goLeft        -> List(Assign(laneChgRequest, LEFT)),
    cancelRequest -> List(Assign(laneChgRequest, CANCEL)),
    b1true        -> List(AssignInMap(decVar, b1, true)),
    b2true        -> List(AssignInMap(decVar, b2, true)),
    b3true        -> List(AssignInMap(decVar, b3, true)),
    b4true        -> List(AssignInMap(decVar, b4, true)),
    b5true        -> List(AssignInMap(decVar, b5, true)),
    b6true        -> List(AssignInMap(decVar, b6, true)),
    b7true        -> List(AssignInMap(decVar, b7, true)),
    b1false       -> List(AssignInMap(decVar, b1, false)),
    b2false       -> List(AssignInMap(decVar, b2, false)),
    b3false       -> List(AssignInMap(decVar, b3, false)),
    b4false       -> List(AssignInMap(decVar, b4, false)),
    b5false       -> List(AssignInMap(decVar, b5, false)),
    b6false       -> List(AssignInMap(decVar, b6, false)),
    b7false       -> List(AssignInMap(decVar, b7, false))
  )
}

object LaneChangeSimulate {
  def apply(): LaneChangeSimulate = new LaneChangeSimulate()
}
