package modelbuilding.models.CatAndMouseModular

import modelbuilding.core._
import modelbuilding.core.simulation.{CodeSimulator, Simulator}

class SimulateCatAndMouseModular extends CodeSimulator{

  import Rooms._

  val cat = "cat"
  val mouse = "mouse"

  override val initState: StateMap = StateMap(cat->R2,mouse->R4)
  override val goalStates: Option[Set[StateMap]] = None
  override val goalPredicate: Option[Predicate] = Some(AND(EQ(cat,R2),EQ(mouse,R4)))

  override val guards: Map[Command,Predicate] = Map(
    c1 -> EQ(cat,R0),
    c2 -> EQ(cat,R1),
    c3 -> EQ(cat,R2),
    c4 -> EQ(cat,R0),
    c5 -> EQ(cat,R3),
    c6 -> EQ(cat,R4),
    c7 -> OR(EQ(cat,R1),EQ(cat,R3)),
    m1 -> EQ(mouse,R0),
    m2 -> EQ(mouse,R2),
    m3 -> EQ(mouse,R1),
    m4 -> EQ(mouse,R0),
    m5 -> EQ(mouse,R4),
    m6 -> EQ(mouse,R3)
  )

  override val actions: Map[Command,List[Action]] = Map(
    c1 -> List(Assign(cat,R1)),
    c2 -> List(Assign(cat,R2)),
    c3 -> List(Assign(cat,R0)),
    c4 -> List(Assign(cat,R3)),
    c5 -> List(Assign(cat,R4)),
    c6 -> List(Assign(cat,R0)),
    c7 -> List(ToggleWithValues(cat,(R1,R3))),
    m1 -> List(Assign(mouse,R2)),
    m2 -> List(Assign(mouse,R1)),
    m3 -> List(Assign(mouse,R0)),
    m4 -> List(Assign(mouse,R4)),
    m5 -> List(Assign(mouse,R3)),
    m6 -> List(Assign(mouse, R0))
  )


}
