package modelbuilding.models.MachineBuffer

import modelbuilding.core._
import modelbuilding.core.modelInterfaces.Simulator

class SimulateMachineBuffer extends Simulator{
  val m1="m1"
  val m2="m2"
  val b="b"
  val initMap=Map(m1->false,m2->false)
  override val initState: StateMap =StateMap(state=initMap)

  override def evalCommandToRun(c: Command, s: StateMap): Option[Boolean] = {
    val pred_l1=EQ(m1,false)
    val pred_l2=EQ(m2,false)
    val pred_u1=EQ(m1,true)
    val pred_u2=EQ(m2,true)


    c match {
      case `load1` => pred_l1.eval(s)
      case `load2` =>pred_l2.eval(s)
      case `unload1` => pred_u1.eval(s)
      case `unload2` => pred_u2.eval(s)



      case `reset` => Some(true)
      case `tou` => Some(true)
    }
  }

  override def translateCommand(c: Command): List[Action] ={
    c match {
      case `load1` => List(Toggle(m1))
      case `load2` => List(Toggle(m2))
      case `unload1` => List(Toggle(m1))
      case `unload2` => List(Toggle(m2))


      case `reset` => initState.getState.toList.map(x => Assign(x._1,x._2))
      case `tou` => List(TouAction)
    }
  }
}
