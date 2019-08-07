package modelbuilding.models.TestUnit

import modelbuilding.core._
import modelbuilding.core.modelInterfaces.Simulator

class SimulateTL extends Simulator {

  val m1 = "m1"
  val m2 = "m2"
  val tu = "tu"

  import modelbuilding.models.TestUnit.Status._
  val initMap = Map(m1->Initial,m2->Initial,tu->Initial)

  override val initState: StateMap = StateMap(state = initMap)

  override def evalCommandToRun(c: Command, s: StateMap): Option[Boolean] = {
    val pred_s1 = EQ(m1,Initial)
    val pred_f1 = EQ(m1,Working)

    val pred_s2 = EQ(m2,Initial)
    val pred_f2 = EQ(m2,Working)

    val pred_test = EQ(tu,Initial)
    val pred_accept = EQ(tu,Working)
    val pred_reject = EQ(tu,Working)

    c match {
      case `start1` => pred_s1.eval(s)
      case `finish1` =>pred_f1.eval(s)
      case `start2` => pred_s2.eval(s)
      case `finish2` =>pred_f2.eval(s)
      case `test` =>pred_test.eval(s)
      case `accept` => pred_accept.eval(s)
      case `reject` => pred_reject.eval(s)


      case `reset` => Some(true)
      case `tau` => Some(true)
    }

  }

  override def translateCommand(c: Command): List[Action] = {
    c match {
      case `start1` => List(Assign(m1,Working))
      case `finish1` => List(Assign(m1,Initial))
      case `start2` => List(Assign(m2,Working))
      case `finish2` => List(Assign(m2,Initial))
      case `test` => List(Assign(tu,Working))
      case `accept` => List(Assign(tu,Initial))
      case `reject` => List(Assign(tu,Initial))


      case `reset` => initState.getState.toList.map(x => Assign(x._1,x._2))
      case `tau` => List(TauAction)
    }
  }


}
