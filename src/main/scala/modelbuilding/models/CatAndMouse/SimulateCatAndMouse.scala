package modelbuilding.models.CatAndMouse

import modelbuilding.core._
import modelbuilding.core.simulation.Simulator

class SimulateCatAndMouse extends Simulator{

  import Occupant._

  val r0 = "r0"
  val r1 = "r1"
  val r2 = "r2"
  val r3 = "r3"
  val r4 = "r4"

  override val initState: StateMap = StateMap(r0->EMPTY,r1->EMPTY,r2->CAT,r3->EMPTY,r4->MOUSE)
  override val goalStates: Option[Set[StateMap]] = None

  override val guards: Map[Command,Predicate] = Map(
    c1 -> EQ(r0,CAT),
    c2 -> EQ(r1,CAT),
    c3 -> EQ(r2,CAT),
    c4 -> EQ(r0,CAT),
    c5 -> EQ(r3,CAT),
    c6 -> EQ(r4,CAT),
    c7 -> OR(EQ(r1,CAT),EQ(r3,CAT)),
    m1 -> EQ(r0,MOUSE),
    m2 -> EQ(r2,MOUSE),
    m3 -> EQ(r1,MOUSE),
    m4 -> EQ(r0,MOUSE),
    m5 -> EQ(r4,MOUSE),
    m6 -> EQ(r3,MOUSE),
  )

  override val actions: Map[Command,List[Action]] = Map(
      c1 -> List(Assign(r1,CAT),Assign(r0,EMPTY)),
      c2 -> List(Assign(r2,CAT),Assign(r1,EMPTY)),
      c3 -> List(Assign(r0,CAT),Assign(r2,EMPTY)),
      c4 -> List(Assign(r3,CAT),Assign(r0,EMPTY)),
      c5 -> List(Assign(r4,CAT),Assign(r3,EMPTY)),
      c6 -> List(Assign(r0,CAT),Assign(r4,EMPTY)),
      c7 -> List(ToggleWithValues(r1,(CAT,EMPTY)),ToggleWithValues(r3,(CAT,EMPTY))),
      m1 -> List(Assign(r2,MOUSE),Assign(r0,EMPTY)),
      m2 -> List(Assign(r1,MOUSE),Assign(r2,EMPTY)),
      m3 -> List(Assign(r0,MOUSE),Assign(r1,EMPTY)),
      m4 -> List(Assign(r4,MOUSE),Assign(r0,EMPTY)),
      m5 -> List(Assign(r3,MOUSE),Assign(r4,EMPTY)),
      m6 -> List(Assign(r0 ,MOUSE),Assign(r3,EMPTY)),
  )

}
