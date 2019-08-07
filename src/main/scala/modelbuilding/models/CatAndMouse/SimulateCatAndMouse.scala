package modelbuilding.models.CatAndMouse

import modelbuilding.core._
import modelbuilding.core.modelInterfaces.Simulator

class SimulateCatAndMouse extends Simulator{

  import Occupant._



  val r0 = "r0"
  val r1 = "r1"
  val r2 = "r2"
  val r3 = "r3"
  val r4 = "r4"

  val initMap = Map(r0->EMPTY,r1->EMPTY,r2->CAT,r3->EMPTY,r4->MOUSE)


  override val initState: StateMap = StateMap(state = initMap)

  override def evalCommandToRun(c: Command, s: StateMap): Option[Boolean] = {

    val pred_c1 = EQ(r0,CAT)
    val pred_c2 = EQ(r1,CAT)
    val pred_c3 = EQ(r2,CAT)
    val pred_c4 = EQ(r0,CAT)
    val pred_c5 = EQ(r3,CAT)
    val pred_c6 = EQ(r4,CAT)
    val pred_c7 = OR(List(EQ(r1,CAT),EQ(r3,CAT)))
    val pred_m1 = EQ(r0,MOUSE)
    val pred_m2 = EQ(r2,MOUSE)
    val pred_m3 = EQ(r1,MOUSE)
    val pred_m4 = EQ(r0,MOUSE)
    val pred_m5 = EQ(r4,MOUSE)
    val pred_m6 = EQ(r3,MOUSE)

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
      case `tou` => Some(true)
    }

  }

  override def translateCommand(c: Command): List[Action] = {

    c match {
      case `c1` => List(Assign(r1,CAT),Assign(r0,EMPTY))
      case `c2` => List(Assign(r2,CAT),Assign(r1,EMPTY))
      case `c3` => List(Assign(r0,CAT),Assign(r2,EMPTY))
      case `c4` => List(Assign(r3,CAT),Assign(r0,EMPTY))
      case `c5` => List(Assign(r4,CAT),Assign(r3,EMPTY))
      case `c6` => List(Assign(r0,CAT),Assign(r4,EMPTY))
      case `c7` => List(ToggleWithValues(r1,(CAT,EMPTY)),ToggleWithValues(r3,(CAT,EMPTY)))
      case `m1` => List(Assign(r2,MOUSE),Assign(r0,EMPTY))
      case `m2` => List(Assign(r1,MOUSE),Assign(r2,EMPTY))
      case `m3` => List(Assign(r0,MOUSE),Assign(r1,EMPTY))
      case `m4` => List(Assign(r4,MOUSE),Assign(r0,EMPTY))
      case `m5` => List(Assign(r3,MOUSE),Assign(r4,EMPTY))
      case `m6` => List(Assign(r0 ,MOUSE),Assign(r3,EMPTY))

      case `reset` => initState.getState.toList.map(x => Assign(x._1,x._2))
      case `tou` => List(TouAction)

    }
  }

}
