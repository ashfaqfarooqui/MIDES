package modelbuilding.core

import grizzled.slf4j.Logging
import modelbuilding.core.LearningType.LearningType
import modelbuilding.core.modelInterfaces.Teacher
import modelbuilding.core.modeling.{Model, Specifications}
import modelbuilding.core.simulators.Simulator
import org.supremica.automata
import scala.collection.JavaConverters._

object SUL {
  def apply(
      model: Model,
      simulator: Simulator,
      specification: Option[Specifications],
      learningType: LearningType,
      acceptsPartialStates: Boolean
    ): SUL = new SUL(model, simulator, specification, learningType, acceptsPartialStates)
}

case class SUL(
    model: Model,
    simulator: Simulator,
    specification: Option[Specifications],
    learningType: LearningType,
    acceptsPartialStates: Boolean = false)
    extends Teacher
    with Logging {

  def getInitState: StateMap               = simulator.initState
  def getGoalStates: Option[Set[StateMap]] = simulator.goalStates
  def getGoalPredicate: Option[Predicate]  = simulator.goalPredicate

  def getNextState(state: StateMap, command: Command): Option[StateMap] = {
    simulator.runCommand(command, state, acceptsPartialStates) match {
      case Right(s) => Some(s)
      case Left(_)  => None
    }
  }

  def getNextState(state: StateMap, commands: Alphabet): List[StateMap] =
    getOutgoingTransitions(state, commands).map(_.target)

  def getOutgoingTransitions(state: StateMap, commands: Alphabet): List[StateMapTransition] =
    commands.events.foldLeft(List.empty[StateMapTransition])(
      (acc: List[StateMapTransition], in: Symbol) =>
        getNextState(state, in.getCommand) match {
          case Some(s) => StateMapTransition(state, s, in) :: acc
          case None    => acc
        }
    )

  def grammarToList(g: Grammar): List[Command] = {
    g match {
      case w: Word   => w.getSequence.map(_.getCommand)
      case s: Symbol => List(s.getCommand)
    }
  }

  def executeSequence(seq: Grammar) = simulator.runListOfCommands(grammarToList(seq), getInitState) match {
    case Right(st) => Some(st)
    case Left(st)  => None
  }

  //Check-> send in a function to evaluate additionally. here I use if to check accepting nature of the reached state
  def isSequenceAllowedInSpec(grammar: Grammar, specName: String): Either[Boolean, automata.State] = {

    val specAutomaton = specification.get.getSupremicaSpecs(specName)
    val s             = specAutomaton.getInitialState
    val alphabet      = specAutomaton.getAlphabet
    val p             = grammar.getSequenceAsString.filterNot(_ == tau.toString).filter(a => alphabet.contains(a.toString))

    def loop(s: automata.State, p: List[String]): Either[Boolean, automata.State] = {
      //debug(s"inloop: at $s and ${p}")
      //debug(s"check ${check(s)}")
      if (p.nonEmpty && s.getOutgoingArcs.asScala.exists(_.getEvent.getLabel == p.head)) {
        loop(s.getOutgoingArcs.asScala.find(_.getEvent.getLabel == p.head).get.getToState, p.tail)
      } else if (p.isEmpty) Right(s)
      else {
        Left(false)
      }
    }
    val allowedInSpec = loop(s, p)

    debug(s"is sequence $grammar allowed in spec $allowedInSpec")
    allowedInSpec
  }

  def isSequenceUnControllableInSpec(sequence: Grammar, alphabet: Alphabet, specName: String): Boolean = {

    val specAutomaton = specification.get.getSupremicaSpecs(specName)
    val pref          = sequence.getAllPrefixes
    val ucEvents      = alphabet.events.filter(_.getCommand.isInstanceOf[Uncontrollable])
    val su            = ucEvents //.flatMap(a=>ucEvents.map(a+_)).flatMap(a=>ucEvents.map(a+_))
    val tu            = pref.flatMap(t => su.map(t + _))
    //val tuNotCtrl = tu.filterNot(x=>isSequenceAllowedInSpec(x,specName,_=>true)).exists(executeSequence(_).isDefined)

    val tuNotCtrl = tu.filterNot(x => isSequenceAllowedInSpec(x, specName).isRight).exists(executeSequence(_).isDefined)
    debug(s"is sequence $sequence controllable $tuNotCtrl")
    tuNotCtrl
  }

  def isAllowedInPlant(g: Grammar): Int = {
    var returnValue: Int = -1
    def evalGoal(st: StateMap) = {
      getGoalPredicate.getOrElse(AlwaysTrue).eval(st).get || getGoalStates.getOrElse(Set.empty).contains(st)
    }
    val resultState = executeSequence(g)
    if (resultState.isDefined) {
      if (evalGoal(resultState.get))
        returnValue = 2
      else returnValue = 1
    } else returnValue = 0
//Include specs
    debug(s"is $g allowed in plant $returnValue")
    returnValue
  }

  def isMember(specName: Option[String])(g: Grammar): Int = {
    val member = if (specName.isDefined) {
      if (!isSequenceUnControllableInSpec(g, model.alphabet, specName.get)) {
        lazy val specAllowed = isSequenceAllowedInSpec(g, specName.get) match {
          case Right(value) => if (specification.get.isAccepting(specName.get, value.getName)) 2 else 1
          case Left(value)  => 0
        }
        isAllowedInPlant(g) min specAllowed
      } else 0
    } else isAllowedInPlant(g) //Plant solver
    debug(s"is $g member $member")
    member
  }

}
