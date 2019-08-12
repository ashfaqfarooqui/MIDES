package modelbuilding.core.modelInterfaces

import modelbuilding.core._
import grizzled.slf4j.Logging


trait Simulator extends Logging{

  val initState: StateMap
  val goalStates: Option[Set[StateMap]]

  val guards: Map[Command,Predicate]
  val actions: Map[Command,List[Action]]

  def evalCommandToRun(c: Command, s: StateMap): Option[Boolean] = {
  c match {
      case `reset` => Some(true)
      case `tau` => Some(true)
      case x if guards contains x => guards(x).eval(s)
      case y => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  def translateCommand(c: Command): List[Action] ={
    c match {
      case `reset` => initState.getState.toList.map(x => Assign(x._1,x._2))
      case `tau` => List(TauAction)
      case x if guards contains x => actions(x)
      case y => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  def runCommand(c:Command, s:StateMap):Either[StateMap,StateMap]={
    if(evalCommandToRun(c,s).get){
      Right(translateCommand(c).foldLeft(s)((st,a)=>a.next(st)))
    }
    else {
      Left(s)
    }
  }


  def runListOfCommands(commands: List[Command],s :StateMap):Either[StateMap,StateMap] ={

    def runList(c:List[Command],ns: StateMap):Either[StateMap,StateMap]= {

      c match {

        case x::xs =>

          runCommand (x, ns) match {
            case Right (n) => runList (xs, n)
            case Left (n) => Left (n)
          }
        case Nil => Right(ns)
      }

    }
    runList(commands,s)

  }


}
