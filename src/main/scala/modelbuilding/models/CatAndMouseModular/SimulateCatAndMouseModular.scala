package modelbuilding.models.CatAndMouseModular

import modelbuilding.core._
<<<<<<< HEAD
import modelbuilding.core.simulation.Simulator
=======
import modelbuilding.core.modelInterfaces.Simulator
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.

class SimulateCatAndMouseModular extends Simulator{

  import Rooms._

  val cat = "cat"
  val mouse = "mouse"

  override val initState: StateMap = StateMap(cat->R2,mouse->R4)
  override val goalStates: Option[Set[StateMap]] = None

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 1. Updated the Simulation structure
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
<<<<<<< HEAD
=======
  override def evalCommandToRun(c: Command, s: StateMap): Option[Boolean] = {

    val pred_c1 = EQ(cat, R0)
    val pred_c2 = EQ(cat,R1)
    val pred_c3 = EQ(cat,R2)
    val pred_c4 = EQ(cat,R0)
    val pred_c5 = EQ(cat,R3)
    val pred_c6 = EQ(cat,R4)
    val pred_c7 = OR(List(EQ(cat,R1),EQ(cat,R3)))
    val pred_m1 = EQ(mouse,R0)
    val pred_m2 = EQ(mouse,R2)
    val pred_m3 = EQ(mouse,R1)
    val pred_m4 = EQ(mouse,R0)
    val pred_m5 = EQ(mouse,R4)
    val pred_m6 = EQ(mouse,R3)

    c match {
      case `c1` => pred_c1.eval(s)
      case `c2` =>pred_c2.eval(s)
      case `c3` => pred_c3.eval(s)
      case `c4` =>pred_c4.eval(s)
      case `c5` => pred_c5.eval(s)
      case `c6` =>pred_c6.eval(s)
      case `c7` => pred_c7.eval(s)
      case `m1` =>pred_m1.eval(s)
      case `m2` =>pred_m2.eval(s)
      case `m3` =>pred_m3.eval(s)
      case `m4` =>pred_m4.eval(s)
      case `m5` =>pred_m5.eval(s)
      case `m6` =>pred_m6.eval(s)

      case `reset` => Some(true)
      case `tau` => Some(true)
    }

  }

  override def translateCommand(c: Command): List[Action] = {

    c match {
      case `c1` => List(Assign(cat,R1))
      case `c2` => List(Assign(cat,R2))
      case `c3` => List(Assign(cat,R0))
      case `c4` => List(Assign(cat,R3))
      case `c5` => List(Assign(cat,R4))
      case `c6` => List(Assign(cat,R0))
      case `c7` => List(ToggleWithValues(cat,(R1,R3)))
      case `m1` => List(Assign(mouse,R2))
      case `m2` => List(Assign(mouse,R1))
      case `m3` => List(Assign(mouse,R0))
      case `m4` => List(Assign(mouse,R4))
      case `m5` => List(Assign(mouse,R3))
      case `m6` => List(Assign(mouse, R0))

      case `reset` => initState.getState.toList.map(x => Assign(x._1,x._2))
      case `tau` => List(TauAction)

    }
  }
>>>>>>> Created a new modular CatAndMouse model. Improved the output of the automata so we can see variables in the state names.
=======
>>>>>>> 1. Updated the Simulation structure

}
