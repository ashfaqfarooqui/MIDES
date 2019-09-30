package modelbuilding.models.TestUnit

import modelbuilding.core._
import modelbuilding.core.simulation.{CodeSimulator, Simulator}

class SimulateTL extends CodeSimulator {

  val m1 = "m1"
  val m2 = "m2"
  val tu = "tu"

  import modelbuilding.models.TestUnit.Status._

  override val initState: StateMap = StateMap(m1->Initial,m2->Initial,tu->Initial)

  override val goalStates: Option[Set[StateMap]] = None
  override val goalPredicate: Option[Predicate] = Some(AND(EQ(m1,Initial),EQ(m2,Initial),EQ(tu,Initial)))


  override val guards: Map[Command,Predicate] = Map(
    start1 -> EQ(m1,Initial),
    finish1 -> EQ(m1,Working),
    start2 -> EQ(m2,Initial),
    finish2 -> EQ(m2,Working),
    test -> EQ(tu,Initial),
    accept -> EQ(tu,Working),
    reject -> EQ(tu,Working),
  )

  override val actions: Map[Command,List[Action]] = Map(
    start1 -> List(Assign(m1,Working)),
    finish1 -> List(Assign(m1,Initial)),
    start2 -> List(Assign(m2,Working)),
    finish2 -> List(Assign(m2,Initial)),
    test -> List(Assign(tu,Working)),
    accept -> List(Assign(tu,Initial)),
    reject -> List(Assign(tu,Initial)),
  )


}
