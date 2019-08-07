package main.scala.models.StickPicking

import core._
import core.modelInterfaces.Simulator

class SimulateSticks(sticks: Int) extends Simulator {

  val Ssticks = "sticks"
  val player = "player"

  val p1 = "p1"
  val p2 = "p2"


  val initMap = Map(Ssticks->sticks, player->p1)
//val goalMap = Map(Ssticks->0, player->p2)

  val goal = AND(List(EQ(player,p1),EQ(Ssticks,0)))
  //val goal = EQ(Ssticks,0)

  val initState = StateMap(state = initMap)
//val goalState = StateMap(state = goalMap)

  def getNoSticks()=
  {
    sticks
  }

  override def translateCommand(c: Command)=
  {
    c match {
      case `e11` => List(Decr(Ssticks,1),Assign(player,p2))
      case `e12` =>List(Decr(Ssticks,2),Assign(player,p2))
      case `e21` =>  List(Decr(Ssticks,1),Assign(player,p1))
      case `e22` => List(Decr(Ssticks,2),Assign(player,p1))
      case `e13` => List(Decr(Ssticks,3),Assign(player,p2))
      case `e23` => List(Decr(Ssticks,3),Assign(player,p1))
      case `reset` => initState.getState.toList.map(x => Assign(x._1,x._2))
      case `tou` => List(TouAction)

    }

  }


  override def evalCommandToRun(c:Command, s: StateMap) ={

    val rem_one = GR(Ssticks,0)
    val rem_two = GR(Ssticks,1)
    val rem_three = GR(Ssticks,2)
    val p1Chance = EQ(player,p1)
    val p2Chance = EQ(player,p2)

    c match {
      case `e11` => AND(List(rem_one,p1Chance)).eval(s)
      case `e12` =>AND(List(rem_two,p1Chance)).eval(s)
      case `e21` => AND(List(rem_one,p2Chance)).eval(s)
      case `e22` =>AND(List(rem_two,p2Chance)).eval(s)
      case `e13` =>AND(List(rem_three,p1Chance)).eval(s)
      case `e23` =>AND(List(rem_three,p2Chance)).eval(s)

      case `reset` => Some(true)
      case `tou` => Some(true)
    }
  }



}
