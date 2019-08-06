package main.scala.models.RobotArm

import core.modelInterfaces._
import core._

class SimulateArm(gridX: Int, gridY:Int ) extends Simulator{

  val x = "x"
  val y = "y"
  val extended = "extended"
  val gripped = "gripped"

  val initMap = Map(x->0,y->0,extended->false,gripped->false)

  val goal = AND(List(EQ(x,0),EQ(y,0),EQ(extended,false),EQ(gripped,false)))
  val initState = StateMap(state = initMap)

  def getGridSize()=
  {
    (gridX,gridY)
  }
  override def translateCommand(c: Command)=
  {
    c match {
      case `left` => List(Decr(x,1))
      case `right` =>List(Incr(x,1))
      case `up` =>  List(Incr(y,1))
      case `down` => List(Decr(y,1))
      case `extend` =>List(Toggle(extended))
      case `retract` =>List(Toggle(extended))
      case `grip` =>List(Toggle(gripped))
      case `release` =>List(Toggle(gripped))
      case `reset` => initState.getState.toList.map(x => Assign(x._1,x._2))
      case `tou` => List(TouAction)

    }

  }

  override def evalCommandToRun(c:Command, s: StateMap) ={
    val newState = translateCommand(c).foldLeft(s)((st,a)=>a.next(st))
    //val newState = s
    val canMove = EQ(extended,false)

    val xPred = AND(List(GREQ(x,0), LEQ(x,gridX),canMove))
    val yPred = AND(List(GREQ(y,0), LEQ(y,gridY),canMove))

    val extPred = EQ(extended,true)
    val retPred = EQ(extended,false)

    val gripPred = AND(List(EQ(gripped,true),EQ(extended,true)))
    val releasePred = AND(List(EQ(gripped,false),EQ(extended,true)))

    c match {
      case `left` => xPred.eval(newState)
      case `right` =>xPred.eval(newState)
      case `up` => yPred.eval(newState)
      case `down` =>yPred.eval(newState)
      case `extend` =>extPred.eval(newState)
      case `retract` =>retPred.eval(newState)
      case `grip` =>gripPred.eval(newState)
      case `release` =>releasePred.eval(newState)
      case `reset` => Some(true)
      case `tou` => Some(true)
    }
  }




}
