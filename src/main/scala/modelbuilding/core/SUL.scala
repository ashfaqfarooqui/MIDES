package modelbuilding.core

import grizzled.slf4j.Logging
import modelbuilding.core.LearningType.LearningType
import modelbuilding.core.modelInterfaces.Teacher
import modelbuilding.core.modeling.{Model, Specifications}
import modelbuilding.core.simulation.Simulator


object SUL {
  def apply(model: Model, simulator: Simulator, specification: Option[Specifications], learningType: LearningType, acceptsPartialStates: Boolean): SUL = new SUL(model, simulator, specification, learningType, acceptsPartialStates)
}

case class SUL(model:Model, simulator: Simulator,specification:Option[Specifications],learningType:LearningType,acceptsPartialStates: Boolean = false) extends Teacher with Logging{

  def getInitState: StateMap = simulator.initState
  def getGoalStates: Option[Set[StateMap]] = simulator.goalStates
  def getGoalPredicate: Option[Predicate] = simulator.goalPredicate

  def getNextState(state: StateMap, command:Command): Option[StateMap] = {
    simulator.runCommand(command, state, acceptsPartialStates) match {
      case Right(s) => Some(s)
      case Left(_) => None
    }
  }

  def getNextState(state:StateMap, commands: Alphabet): List[StateMap] =
    getOutgoingTransitions(state, commands).map(_.target)

  def getOutgoingTransitions(state: StateMap, commands: Alphabet): List[StateMapTransition] =
    commands.events.foldLeft(List.empty[StateMapTransition])((acc: List[StateMapTransition], in: Symbol) =>
      getNextState(state, in.getCommand) match {
        case Some(s) => StateMapTransition(state, s, in) :: acc
        case None => acc
      })

  def grammarToList(g: Grammar):List[Command]={
    g match {
      case w : Word => w.getSequence.map(_.getCommand)
      case s : Symbol => List(s.getCommand)
    }
  }

  def executeSequence(seq:Grammar) = simulator.runListOfCommands(grammarToList(seq), getInitState) match {
    case Right(st) => Some(st)
    case Left(st) => None
  }


  def isMember(g:Grammar):Int= {
    var returnValue:Int = -1
    def evalGoal(st:StateMap) = {
      getGoalPredicate.getOrElse(AlwaysTrue).eval(st).get || getGoalStates.getOrElse(Set.empty).contains(st)
    }
    val resultState = executeSequence(g)
    if(resultState.isDefined) {
      if(evalGoal(resultState.get))
        returnValue=2
      else returnValue=1
    }else returnValue=0
//Include specs
    returnValue
  }


}
