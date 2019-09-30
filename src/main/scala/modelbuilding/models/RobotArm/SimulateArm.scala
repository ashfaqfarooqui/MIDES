package modelbuilding.models.RobotArm

import modelbuilding.core._
import modelbuilding.core.simulation.{CodeSimulator, Simulator}

class SimulateArm(gridX: Int, gridY:Int ) extends CodeSimulator{

  val x = "x"
  val y = "y"
  val extended = "extended"
  val gripped = "gripped"

  val initState = StateMap(x->0,y->0,extended->false,gripped->false)

  //val goal = AND(List(EQ(x,0),EQ(y,0),EQ(extended,false),EQ(gripped,false)))
  override val goalStates: Option[Set[StateMap]] =
    Some(Set(StateMap(x->0,y->0,extended->false,gripped->false)))

  def getGridSize()=
  {
    (gridX,gridY)
  }

  // Not applicable since evalCommandToRun requires some special treatment (implementation of predicate `NEXT`)
  override val guards: Map[Command,Predicate] = Map.empty[Command,Predicate]

  override def evalCommandToRun(c:Command, s: StateMap, acceptPartialStates: Boolean): Option[Boolean] ={

    val newState = actions(c).foldLeft(s)((st,a)=>a.next(st))
    //val newState = s
    val canMove = EQ(extended,false)

    val xPred = AND(GREQ(x,0), LEQ(x,gridX), canMove)
    val yPred = AND(GREQ(y,0), LEQ(y,gridY), canMove)

    val extPred = EQ(extended,true)
    val retPred = EQ(extended,false)

    val gripPred = AND(EQ(gripped,true),EQ(extended,true))
    val releasePred = AND(EQ(gripped,false),EQ(extended,true))

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
      case `tau` => Some(true)
    }
  }

  override val actions: Map[Command,List[Action]] = Map(
    left -> List(Decr(x,1)),
    right ->List(Incr(x,1)),
    up ->  List(Incr(y,1)),
    down -> List(Decr(y,1)),
    extend ->List(Toggle(extended)),
    retract ->List(Toggle(extended)),
    grip ->List(Toggle(gripped)),
    release ->List(Toggle(gripped)),
    reset -> initState.getState.toList.map(x => Assign(x._1,x._2)),
    tau -> List(TauAction),
  )
}
