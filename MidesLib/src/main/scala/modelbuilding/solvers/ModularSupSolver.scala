package modelbuilding.solvers
import grizzled.slf4j.Logging
import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core.{
  AND,
  Alphabet,
  AlwaysTrue,
  Automata,
  Automaton,
  EQ,
  OR,
  SUL,
  State,
  StateMap,
  StateMapTransition,
  StateSet,
  Symbol,
  Transition,
  Uncontrollable
}
import modelbuilding.solvers.ModularSupSolver._
import modelbuilding.solvers.MonolithicSupSolver.extendStateMap
import org.supremica.automata

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.immutable.Queue

object ModularSupSolver {
  def getRequiredModules(
      m: ModularModel,
      spec: automata.Automaton
    ): Map[String, Alphabet] = {
    m.eventMapping filter { case (k: String, v: Alphabet) =>
      v.events.exists(x =>
        x.getCommand
          .isInstanceOf[Uncontrollable] && spec.getAlphabet.contains(x.toString)
      )
    }
  }
  def aggregateModulesForModel(
      m: ModularModel,
      sp: Set[automata.Automaton]
    ): Map[automata.Automaton, Alphabet] = {
    sp.map { s =>
      val specAlphabet = s.getAlphabet
      //This should work for the base case -- we get a supervisor, but not a supremal controllable supervisor
      def selectedAlphabet = getRequiredModules(m, s).values.toSet
      val selAlphabet = selectedAlphabet.foldLeft(Set.empty[Symbol]) { (acc, i) =>
        acc union i.events
      } union m.alphabet.events.filter(a => specAlphabet.contains(a.toString))

      println(s"Selected alphabet: ${s -> selAlphabet}")
      //Dirty hack, Alphabet does not take a set as is.
      s -> new Alphabet(selAlphabet)
    }.toMap
  }

  //Remove spec variable
  def getReducedState(
      model: ModularModel,
      spec: automata.Automaton
    )(sp: StateMap
    ): StateMap = {
    def reducedVariables: Set[StateSet] =
      model.stateMapping.view
        .filterKeys(getRequiredModules(model, spec).contains)
        .toMap
        .values
        .toSet
    StateMap(
      sp.name,
      states = sp.states.view
        .filterKeys(
          (reducedVariables.flatMap(_.states) + spec.getName).contains
        )
        .toMap
    )
  }

}

class ModularSupSolver(_sul: SUL) extends BaseSolver with Logging {

  val _model = _sul.model
  assert(
    _sul.specification.isDefined,
    "modelbuilder.solver.ModularSupSolver requires a specification."
  )
  assert(
    _model.isModular,
    "modelbuilder.solver.ModularSupSolver requires a modular model."
  )

  info("Initializing ModularSupSolver")
  val specs: Set[automata.Automaton] =
    _sul.specification.get.getSupremicaSpecs.values.toSet //SupremicaWatersSystem(_sul.specification.get).getSupremicaSpecs.asScala.toSet

  specs.foreach(s => info(s"Read spec for ${s.getName}"))

  val model: ModularModel = _model.asInstanceOf[ModularModel]
  val initState: StateMap = extendStateMap(specs, _sul.getInitState)

  val initQueue: Queue[StateMap] = Queue(initState)
  val moduleMapping: Map[automata.Automaton, Alphabet] =
    aggregateModulesForModel(model, specs)
  println(s"module mapping $moduleMapping")

  def initMaps[T]: Map[String, Set[T]] =
    moduleMapping.keySet.map(_.getName -> Set.empty[T]).toMap

  private var forbiddenStates: Map[String, Set[StateMap]] = initMaps[StateMap]

  def getNextSpecState(
      sp: automata.Automaton,
      st: StateMap,
      c: Symbol
    ): Option[StateMap] = {
    if (!sp.getAlphabet.contains(c.toString)) {
      return Some(st)
    }
    val specStateInMap = st.getKey(sp.getName).get.asInstanceOf[String]
    val currSpecState  = sp.getStateSet.getState(specStateInMap)
    //since we know the spec is deterministic there can exist just one or none transitions
    val theTransition = currSpecState.getOutgoingArcs.asScala
      .filter(_.getSource.getName == specStateInMap)
      .filter(_.getEvent.getName == c.toString)
    assert(theTransition.size <= 1, "spec is non-deterministic")
    if (theTransition.isEmpty)
      None
    else {
      Some(st.next(sp.getName, theTransition.head.getTarget.getName))
    }
  }

  @tailrec
  final def explore(
      queue: Queue[StateMap],
      moduleStates: Map[String, Set[StateMap]],
      visitedSet: Map[String, Set[StateMap]],
      arcs: Map[String, Set[StateMapTransition]]
    ): (Map[String, Set[StateMap]], Map[String, Set[StateMapTransition]]) = {
    info(s"Starting to explore with queue size ${queue.size}")
    if (queue.isEmpty) {
      (moduleStates, arcs)
    } else {
      var transitions: Map[String, Set[StateMapTransition]] = arcs
      var visited: Map[String, Set[StateMap]]               = visitedSet
      var mStates: Map[String, Set[StateMap]]               = moduleStates
      val currState                                         = queue.dequeue
      var newQueue                                          = currState._2

      for (m <- moduleMapping.keySet) {

        val module                             = m.getName
        def stateReducer: StateMap => StateMap = getReducedState(model, m)

        visited += (module -> (visited(module) + currState._1))
        //info(s"visited $visited")

        if (moduleStates(module).contains(stateReducer(currState._1))) {
          //only iterate over reachable states

          val reachedStates = moduleMapping(m).events
            .map { e =>
              _sul.getNextState(currState._1, e.getCommand) match {
                case Some(value) =>
                  getNextSpecState(m, value, e) match {
                    case Some(v) =>
                      if (!forbiddenStates(module).contains(stateReducer(currState._1))) {
                        transitions += (module -> (transitions(
                          module
                        ) + StateMapTransition(
                          stateReducer(currState._1),
                          stateReducer(v),
                          e
                        )))
                        info(
                          s"found transition: ${StateMapTransition(stateReducer(currState._1), stateReducer(v), e)}"
                        )
                        Some(v)
                      } else {
                        None
                      }
                    case _ =>
                      if (e.getCommand.isInstanceOf[Uncontrollable]) {
                        info(s"found forbidden state: ${currState._1}")
                        forbiddenStates += (module -> (forbiddenStates(
                          module
                        ) + stateReducer(
                          currState._1
                        )))
                      }
                      None
                  }
                case _ =>
                  None
              }
            }
            .filter(_.isDefined)
            .map(_.get)

          info(s"computed reached states of size: ${reachedStates.size}")
          mStates = mStates + (module -> (mStates(module) ++ reachedStates.map(
            stateReducer
          )))

          newQueue = reachedStates
            .diff(visited(module))
            .filterNot(a => newQueue.contains(a))
            .foldLeft(newQueue) { (q, v) =>
              q :+ v
            }
        }
      }

      explore(newQueue, mStates, visited, transitions)

    }
  }

  def initModuleState: Map[String, Set[StateMap]] =
    moduleMapping.keySet
      .map(m => m.getName -> Set(getReducedState(model, m)(initState)))
      .toMap

  println("############")
  println(initQueue)
  initModuleState foreach println
  initMaps[StateMap] foreach println
  initMaps[StateMapTransition] foreach println
  println("####################################")

  val (mStates, mTransitions) =
    explore(initQueue, initModuleState, initMaps[StateMap], initMaps[StateMapTransition])

  for (m <- moduleMapping.keySet) {
    info(s"nbr of states  ${mStates(m.getName).size}")
    info(s"nbr of Trans  ${mTransitions(m.getName).size}")
  }

//we shouldnt have to do this
  def getStatesFromTransitions(t: Set[StateMapTransition]): Set[StateMap] = {
    t.foldLeft(Set(initState)) { (s, t) =>
      s + t.target
    }
  }

  def mapStates(states: Set[StateMap]): Map[StateMap, State] = {
    // states.zipWithIndex.toMap.map{case(sm,i)=> (sm,if(sm.equals(initState)) State(s"init") else State(s"s${i}"))}
    states.map(s => (s, State(s.toString))).toMap
  }

  def mapTransitions(
      stateMapping: Map[StateMap, State],
      trans: Set[StateMapTransition]
    ): Set[Transition] = {
    trans.map { t =>
      Transition(stateMapping(t.source), stateMapping(t.target), t.event)
    }
  }

  override def getAutomata: Automata = {

    val modules = moduleMapping.keySet.map { m =>
      val module                             = m.getName
      def stateReducer: StateMap => StateMap = getReducedState(model, m)
      val mappedStates = mapStates(
        mStates(module) union forbiddenStates(module) union getStatesFromTransitions(
          mTransitions(module)
        )
      )
      info(s"mapped states: $mappedStates")
      val mappedTransitions = mapTransitions(mappedStates, mTransitions(module))
      val fbdStates =
        if (forbiddenStates(module).nonEmpty)
          Some(forbiddenStates(module) map mappedStates)
        else None

      val specGoal =
        if (m.hasAcceptingState)
          m.getStateSet.asScala
            .filter(_.isAccepting)
            .map(a => EQ(m.getName, a.getName))
            .toList
        else List(AlwaysTrue)
      val extendedGoalPredicate =
        AND(_sul.getGoalPredicate.getOrElse(AlwaysTrue), OR(specGoal))
      val markedStateSet = mStates(module).filter(
        extendedGoalPredicate.eval(_, _sul.acceptsPartialStates).get
      ) map mappedStates
      val markedStates = if (markedStateSet.isEmpty) None else Some(markedStateSet)

      Automaton(
        module,
        mappedStates.values.toSet,
        moduleMapping(m),
        mappedTransitions,
        mappedStates(stateReducer(initState)),
        markedStates,
        fbdStates
      )

    }
    Automata(modules)
  }

}
