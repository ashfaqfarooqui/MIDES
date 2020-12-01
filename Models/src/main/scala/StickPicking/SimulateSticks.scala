package StickPicking

import modelbuilding.core._
import modelbuilding.core.interfaces.simulator.CodeSimulator

class SimulateSticks(sticks: Int) extends CodeSimulator {

  val Ssticks = "sticks"
  val player  = "player"

  val p1 = "p1"
  val p2 = "p2"

  val initMap = Map()
//val goalMap = Map(Ssticks->0, player->p2)

  val goal = AND(List(EQ(player, p1), EQ(Ssticks, 0)))
  //val goal = EQ(Ssticks,0)

  override val initState = StateMap(Ssticks -> sticks, player -> p1)

  override val goalStates: Option[Set[StateMap]] =
    Some(Set(StateMap(Ssticks -> 0, player -> p1)))

  def getNoSticks() = {
    sticks
  }

  override val guards: Map[Command, Predicate] = {
    val rem_one   = GR(Ssticks, 0)
    val rem_two   = GR(Ssticks, 1)
    val rem_three = GR(Ssticks, 2)
    val p1Chance  = EQ(player, p1)
    val p2Chance  = EQ(player, p2)
    Map(
      e11 -> AND(rem_one, p1Chance),
      e12 -> AND(rem_two, p1Chance),
      e21 -> AND(rem_one, p2Chance),
      e22 -> AND(rem_two, p2Chance),
      e13 -> AND(rem_three, p1Chance),
      e23 -> AND(rem_three, p2Chance)
    )
  }

  override val actions: Map[Command, List[Action]] = Map(
    e11 -> List(Decr(Ssticks, 1), Assign(player, p2)),
    e12 -> List(Decr(Ssticks, 2), Assign(player, p2)),
    e21 -> List(Decr(Ssticks, 1), Assign(player, p1)),
    e22 -> List(Decr(Ssticks, 2), Assign(player, p1)),
    e13 -> List(Decr(Ssticks, 3), Assign(player, p2)),
    e23 -> List(Decr(Ssticks, 3), Assign(player, p1))
  )

}
