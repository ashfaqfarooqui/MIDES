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
//  println(s"Iteration: 1")
  while ( {
    count += 1
    if (count%100 == 0) println(s"Iteration: $count")
    queue.nonEmpty
  }) {

    val state = queue.head
    queue = queue.tail

    val outgoingTransitionsInPlant: List[StateMapTransition] = simulator.getOutgoingTransitions(state, model.alphabet)

    for ( t <- outgoingTransitionsInPlant )  {

      // filter out those modules that still is part of the state
      val remainingModules = modules.filter(m => state.specs.contains(m.name))

      // check what states have changed in transitions `t`
      val changedStates = t.target.state.keySet.filter(k => t.source.state(k) != t.target.state(k))

      // Find the target states of the transitions in the specifications, caused by executing the event in current state
      val specTargetStates = specifications.evalTransition(t, remainingModules.map(_.name))

      // if event is uncontrollable, those modulesthat try to block it should be forbidden
      if (!t.event.isControllable)
        remainingModules.filter( m => specTargetStates(m.name).isEmpty ).foreach( m => moduleForbidden(m.name) += getReducedStateMap(state, m))

      // filter out modules that should include this transitions, being those that (1 AND (2 OR 3)):
      // 1) does not have the transition blocked by the spec, i.e. their name remains in specTargetStates
      // 2) the state of the spec is changed by the transition
      // 3) at least one state variable is changed by the transition
      val concernedModules = remainingModules.filter { m =>
        specTargetStates(m.name).nonEmpty &&
          (state.specs(m.name) != specTargetStates(m.name).getOrElse("") || m.stateSet.states.intersect(changedStates).nonEmpty)
      }

      // update the transition with the target states of the specifications
      val trans = StateMapTransition(t.source, StateMap(states=t.target.state, specs=specTargetStates.filter(_._2.nonEmpty).mapValues(_.get)), t.event)

      var newStateFound = false
      for (m <- concernedModules) {
        specTargetStates(m.name) match {
          case None =>

          case Some(_) =>
            val tReduced = getReducedStateMapTransition(trans, m)
            moduleTransitions(m.name) += tReduced
            if (!(moduleStates(m.name) contains tReduced.target)) {
              moduleStates(m.name) += tReduced.target
              newStateFound = true
            }
        }
      }
      if (newStateFound) queue = trans.target :: queue
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
  println("DONE!")
  println(s"Number of iterations: $count")


  override def getAutomata: Automata = {

    /*println("#########")
    println(initState)
    for (m <- modules) {
      println("#")
      println(m.name)
      println(getReducedStateMap(initState,m))
      println("##")
      moduleStates(m.name) foreach println
      println("###")
      moduleForbidden(m.name) foreach println
      println("####")
      moduleTransitions(m.name) foreach println
    }*/

    val automatons = for {
      m <- modules
      _ = println(s"Processing: " + m.name)
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
