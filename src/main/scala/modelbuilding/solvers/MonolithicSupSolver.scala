package modelbuilding.solvers
import grizzled.slf4j.Logging
import modelbuilding.core.interfaces.modeling.{ModularModel, MonolithicModel}
import modelbuilding.core.{
  AND,
  AlwaysTrue,
  Automata,
  Automaton,
  EQ,
  OR,
  SUL,
  State,
  StateMap,
  StateMapTransition,
  Symbol,
  Transition,
  Uncontrollable
}
import modelbuilding.solvers.MonolithicSupSolver._
import org.supremica._

import scala.collection.JavaConverters._
import scala.collection.immutable.Queue

object MonolithicSupSolver {
  def extendStateMap(sp: Set[automata.Automaton], st: StateMap): StateMap = {
    StateMap(
      states = st.states ++ sp.map(s => s.getName -> s.getInitialState.getName).toMap
    )
  }

}

class MonolithicSupSolver(_sul: SUL) extends BaseSolver with Logging {

  assert(
    _sul.specification.isDefined,
    "modelbuilder.solver.MonolithicSupSolver requires a specification model."
  )
  info("Initializing SupSolver")

  val specs = _sul.specification.get //SupremicaWatersSystem(_model.specFilePath.get).getSupremicaSpecs.asScala.toSet
  specs.addSynchronizedSpec
  //Need to use full spec...
  println(s"specs $specs")
  val _model = _sul.model

  // val spec = specs.head

  val spec = specs.getSupremicaSpecs(specs.syncSpecName)
  info(s"Read Specifications ${spec}")

  val model = if (_model.isModular) {
    _model.asInstanceOf[ModularModel]
  } else _model.asInstanceOf[MonolithicModel]

  val events: Set[Symbol] = model.alphabet.events
  val initState           = extendStateMap(Set(spec), _sul.getInitState)

  //experimenting with monolithin first

  val queue: Queue[StateMap] = Queue(initState)

  def getNextSpecState(
      sp: automata.Automaton,
      st: StateMap,
      c: Symbol
    ): Option[StateMap] = {
    debug(s"getting next spec state for $sp, $st with symb $c")
    val specStateInMap = st.states(sp.getName).asInstanceOf[String]
    debug(s"current state in map $specStateInMap")
    val currSpecState = sp.getStateSet.getState(specStateInMap)
    debug(s"current state ${currSpecState.getOutgoingArcs}")

    //since we know the spec is deterministic there can exist just one or none transitions
    val theTransition = currSpecState.getOutgoingArcs.asScala
      .filter(_.getSource.getName == specStateInMap)
      .filter(_.getEvent.getName == c.toString)
    debug(s"the transition $theTransition")
    if (theTransition.isEmpty)
      None
    else {
      Some(st.next(sp.getName, theTransition.head.getTarget.getName))
    }
  }

  private var forbiddedStates: Set[StateMap] = Set.empty[StateMap]

  def explore(
      queue: Queue[StateMap],
      visitedSet: Set[StateMap],
      arcs: Set[StateMapTransition]
    ): Set[StateMapTransition] = {
    debug("Starting to explore...")
    if (queue.isEmpty) {
      arcs
    } else {
      var transitions: Set[StateMapTransition] = arcs
      val currState                            = queue.dequeue
      val visited                              = visitedSet + currState._1

      info(s"Queue size ${queue.size}")
      info(s"VisitedSet size ${visited.size}")

      val reachedStates = events
        .map(
          e =>
            _sul.getNextState(currState._1, e.getCommand) match {
              //getNextSpecState updates the state variable in the statemap. hence use the new current state.Mo
              case Some(value) =>
                if (spec.getAlphabet.contains(e.getCommand.toString)) {
                  getNextSpecState(spec, value, e)
                } else { Some(value) } match {
                  case Some(v) =>
                    if (!forbiddedStates.contains(currState._1)) {
                      transitions = transitions + StateMapTransition(currState._1, v, e)
                      Some(v)
                    } else {
                      None
                    }
                  case _ =>
                    if (e.getCommand.isInstanceOf[Uncontrollable]) {
                      forbiddedStates = forbiddedStates + currState._1
                    }
                    None
                }
              case _ =>
                None
            }
        )
        .filter(_.isDefined)
        .map(_.get)

      info(s"reached states from ${currState._1} are ${reachedStates.size}")

      val updq = reachedStates
        .diff(visited)
        .filterNot(a => currState._2.contains(a))
        .foldLeft(currState._2) { (q, v) =>
          q :+ v
        }
      info(s"upd Size: ${updq.size}")
      explore(updq, visited, transitions)
    }

  }

  def getStatesFromTransitions(t: Set[StateMapTransition]): Set[StateMap] = {
    t.foldLeft(Set(initState)) { (s, t) =>
      s + t.target
    }
  }

  def mapStates(states: Set[StateMap]): Map[StateMap, State] = {
    states.zipWithIndex.toMap.map {
      case (sm, i) => (sm, if (sm.equals(initState)) State("init") else State(s"s$i"))
    }
  }

  def mapTransitions(trans: Set[StateMapTransition]): Set[Transition] = {
    trans.map { t =>
      Transition(mappedStates(t.source), mappedStates(t.target), t.event)
    }
  }

  info("Starting to build the models using MonolithicSolver")

  //val extendedGoal = if(spec.hasAcceptingState) spec.getStateSet.asScala.filter(_.isAccepting).map(a=>spec->a.getName).map(_=>sul.getGoalStates.getOrElse(State)) else sul.getGoalStates

  val specGoal =
    if (spec.hasAcceptingState)
      spec.getStateSet.asScala
        .filter(_.isAccepting)
        .map(a => EQ(spec.getName, a.getName))
        .toList
    else List(AlwaysTrue)
  val extendedGoalPredicate =
    AND(_sul.getGoalPredicate.getOrElse(AlwaysTrue), OR(specGoal))
  val transitions         = explore(queue, Set.empty[StateMap], Set.empty[StateMapTransition])
  val allStatesAsStateMap = getStatesFromTransitions(transitions) union forbiddedStates
  val mappedStates        = mapStates(allStatesAsStateMap)
  val mappedTransitions   = mapTransitions(transitions)
  val fbdStates =
    if (!forbiddedStates.isEmpty) Some(forbiddedStates map mappedStates) else None
  val markedStateSet = allStatesAsStateMap.filter(extendedGoalPredicate.eval(_).get) map mappedStates
  val markedStates   = if (markedStateSet.isEmpty) None else Some(markedStateSet)

  info(s"forbiddedStates states: $fbdStates")
  // val aut= Automaton(mappedStates.values.toSet + State("dump:"),createTransitionTable(mappedStates,transitions),model.alphabet,mappedStates(initState),None,None)

  override def getAutomata: Automata = {
    Automata(
      Set(
        Automaton(
          model.name,
          mappedStates.values.toSet,
          model.alphabet,
          mappedTransitions,
          mappedStates(initState),
          markedStates,
          fbdStates
        )
      )
    )
  }

}
