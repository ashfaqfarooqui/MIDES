/*

Second version without partial states.

A brute force BFS that split the result into modules rather than a monolithic plant.

*/

package modelbuilding.solvers


import modelbuilding.core._
import modelbuilding.core.modeling.{Model, ModularModel, Module, Specifications}
import modelbuilding.core.simulation.SUL

import scala.collection.mutable

import FrehageModularSupSynthesis._

object FrehageModularSupSynthesis {

    def getReducedStateMap(state: StateMap, module: Module): StateMap =
      StateMap(
        name = state.name,
        state = state.state.filterKeys(s => module.stateSet.states.contains(s)),
        specs = state.specs.filterKeys(s => module.specs.contains(s))
      )

    def getReducedStateMapTransition(t: StateMapTransition, module: Module): StateMapTransition =
      StateMapTransition(getReducedStateMap(t.source, module), getReducedStateMap(t.target, module), if (module.alphabet.events contains t.event) t.event else Symbol(tau))

}

class FrehageModularSupSynthesis(_model: Model) extends BaseSolver {

  throw new NotImplementedError("FrehageModularSupSynthesis is not yet done...")

  assert(_model.hasSpecs, "modelbuilder.solver.ModularSupSolver requires a specification.")
  assert(_model.isModular, "modelbuilder.solver.FrehageModularSupSynthesis requires a modular model.")


  private val model = _model.asInstanceOf[ModularModel]
  private val specifications = _model.asInstanceOf[Specifications]
  private val simulator: SUL = model.simulation

  private val modules: Set[Module] = specifications.getSupervisorModules
  private val initState: StateMap = specifications.extendStateMap(simulator.getInitState)

  private var queue: List[StateMap] = List(initState)

  private val moduleStates: Map[String, mutable.Set[StateMap]] = modules.map(m => m.name -> mutable.Set(getReducedStateMap(initState,m))).toMap
  private val moduleForbidden: Map[String, mutable.Set[StateMap]] = modules.map(m => m.name -> mutable.Set.empty[StateMap]).toMap
  private val moduleTransitions: Map[String, mutable.Set[StateMapTransition]] = modules.map(_.name -> mutable.Set.empty[StateMapTransition]).toMap

  // One queue for each module to track new states that should be explored.
  var count = 0
  // Loop until all modules are done exploring new states
  println(s"Iteration: 1")
  while ( {
    count += 1
    if (count%100 == 0) println(s"Iteration: $count")
    queue.nonEmpty
  }) {

    val state = queue.head
    queue = queue.tail

    val outgoingTransitionsInPlant: List[StateMapTransition] = simulator.getOutgoingTransitions(state, model.alphabet)

    for ( t <- outgoingTransitionsInPlant )  {

      // check what states have changed in transitions `t`
      val changedStates = t.target.state.keySet.filter(k => t.source.state(k) != t.target.state(k))

      // filter out modules that should include this transitions, being those that (1 AND (2 OR 3)):
      // 1) those specs that is still present in the outgoing state
      // 2) have one of there state variables changed by the transition
      // 3) have the event in their alphabet (indicating a transition in the spec or a self loop)
      val concernedModules = modules.filter( m =>
        state.specs.contains(m.name) && (m.stateSet.states.intersect(changedStates).nonEmpty || m.alphabet.events.contains(t.event)) )

      println(s"### $state")
//      val nextSpecState = specifications.evalTransition(t, concernedModules.map(_.name))
//
//      nextSpecState foreach println
      /*var newStateFound = false
      for (m <- concernedModules) {

        val status = specifications.evalTransition(t, m.specs)
        println(status)
//        val tReduced = getReducedStateMapTransition(t, m)
//        moduleTransitions(m.name) += tReduced
//        if (!(moduleStates(m.name) contains tReduced.target)) {
//          moduleStates(m.name) += tReduced.target
//          newStateFound = true
//        }
      }
      if (newStateFound) queue = t.target :: queue*/
    }

/*    val uncontrollableModules = modules.filter(m => outgoingTransitionsInPlant.filter(!_.event.isControllable).exists{ t =>
       m.alphabet.events.contains(t.event) && (!outgoingTransitionsInSpecs.contains(t.event) || !outgoingTransitionsInSpecs(t.event).contains(m.name))
    })
    uncontrollableModules.foreach( m => moduleForbidden(m.name) += getReducedStateMap(s, m) )

    val filteredTransitinos: List[StateMapTransition] =
      outgoingTransitionsInPlant.filter(t => filteredCommands.events.contains(t.event))

    val updatedTargets: Map[Symbol, StateMap]  =
      filteredTransitinos.map(t => t.event -> StateMap(state=t.target.state,specs=outgoingTransitionsInSpecs(t.event))).toMap

    val next = filteredTransitinos.map(t => StateMapTransition(t.source, updatedTargets(t.event), t.event))

    next.foreach { t =>
      val changedVars = t.target.state.keySet.filter(k => t.source.state(k) != t.target.state(k))
      var newStateFound = false
      for (m <- modules if !uncontrollableModules.contains(m) && (m.stateSet.states intersect changedVars).nonEmpty) {
        val tReduced = getReducedStateMapTransition(t, m)
        moduleTransitions(m.name) += tReduced
        if (!(moduleStates(m.name) contains tReduced.target)) {
          moduleStates(m.name) += tReduced.target
          newStateFound = true
        }
      }
      if (newStateFound) queue = t.target :: queue
    }*/

  }
  println(s"DONE! Number of iterations: $count")


  override def getAutomata: Automata = {


    val automatons = for {
      m <- modules
      nonLocalVariables = moduleTransitions(m.name).filter(_.event.getCommand == tau).flatMap(t => t.source.state.keys.filter(k => t.source.state(k) != t.target.state(k)))
      //      nonLocalVariables = Set.empty[String]
      states: Map[StateMap, State] = moduleStates(m.name).map( s => {
        val state = StateMap(s.state.filterKeys(k => !nonLocalVariables.contains(k)),s.specs)
        val name = (
          (if (state.state.forall{ case (k,v) => initState.state(k) == v } &&
            state.specs.forall{ case (k,v) => initState.specs(k) == v }) "INIT: " else "")
          + state.toString )
        (getReducedStateMap(s,m), State(name))
      }).toMap
      transitions: Set[Transition] = moduleTransitions(m.name).filterNot(_.event.getCommand == tau).map(getReducedStateMapTransition(_,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
      //      transitions: Set[Transition] = moduleTransitions(m).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
      alphabet: Alphabet = m.alphabet
      iState: State = states(getReducedStateMap(initState, m) )
      fStates: Option[Set[State]] = simulator.getGoalStates match {
        case Some(gs) => Some(gs.map( s => states(getReducedStateMap(s, m)) ))
        case None => None
      }
      forbiddenStates = moduleForbidden(m.name).map(states(_)) match {
        case x if x.nonEmpty => Some(x.toSet)
        case _ => None
      }
    } yield Automaton(m.name, states.values.toSet, alphabet, transitions, iState, fStates, forbiddenStates)
    Automata(automatons)
//    Automata(Set.empty[Automaton])
  }

}
