package modelbuilding.core.modelInterfaces

import modelbuilding.core.{Action, Command, StateMap}
import grizzled.slf4j.Logging


trait Simulator extends Logging{
  val initState:StateMap
  def evalCommandToRun(c:Command, s: StateMap):Option[Boolean]
  def translateCommand(c: Command):List[Action]

  def runCommand(c:Command, s:StateMap):Either[StateMap,StateMap]={
    if(evalCommandToRun(c,s).get){
      Right(translateCommand(c).foldLeft(s)((st,a)=>a.next(st)))
    }
    else
      Left(s)
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
