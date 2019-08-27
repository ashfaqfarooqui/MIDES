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
      states = state.states.filterKeys(s => module.stateSet.states.contains(s)),
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

  private val specModules: Set[Module] = specifications.getSupervisorModules
  private val remainingPlantModules: Set[String] = model.modules.filterNot(m => specModules.exists(s => model.eventMapping(m).events.subsetOf(s.alphabet.events)))
  private val plantModules: Set[Module] = remainingPlantModules.map(m => Module(m, model.stateMapping(m), model.eventMapping(m)))
//  private val plantModules: Set[Module] = Set.empty[Module]
  private val modules: Set[Module] = specModules  ++ plantModules

  modules foreach println

  private val initState: StateMap = specifications.extendStateMap(simulator.getInitState)

  private var queue: List[StateMap] = List(initState)
  private val history: mutable.Set[StateMap] = mutable.Set(initState)

  private val moduleStates: Map[String, mutable.Set[StateMap]] = modules.map(m => m.name -> mutable.Set(getReducedStateMap(initState,m))).toMap
  private val moduleForbidden: Map[String, mutable.Set[StateMap]] = modules.map(m => m.name -> mutable.Set.empty[StateMap]).toMap
  private val moduleTransitions: Map[String, mutable.Set[StateMapTransition]] = modules.map(_.name -> mutable.Set.empty[StateMapTransition]).toMap

  var count = 0
  // Loop until all modules are done exploring new states
  while ( {
    count += 1
    if (count%1000 == 0) println(s"Iteration: $count, queue size: ${queue.size}")
    queue.nonEmpty
  }) {

    val state = queue.head
    queue = queue.tail

    val outgoingTransitionsInPlant: List[StateMapTransition] = simulator.getOutgoingTransitions(state, model.alphabet)

    for ( t <- outgoingTransitionsInPlant )  {

      // filter out those specifications that still is part of the state
      val remainingSpecifications = modules.filter(m => state.specs.contains(m.name))

      // Find the target states of the transitions in the specifications, caused by executing the event in current state
      val specTargetStates = specifications.evalTransition(t, remainingSpecifications.map(_.name))

      // if event is uncontrollable, those specifications that try to block it should be forbidden
      if (!t.event.isControllable) {
        remainingSpecifications.filter(m => specTargetStates(m.name).isEmpty).foreach{ m =>
            moduleForbidden(m.name) += getReducedStateMap(state, m)
        }
      }

      // filter out modules that should include this transitions, being those that (1 AND (2 OR 3)):
      // 1) does not have the transition blocked by the spec, i.e. their name remains in specTargetStates
      // 2) the state of the spec is changed by the transition
      // 3) at least one state variable is changed by the transition
      val filteredSpecs = remainingSpecifications.filter { m => specTargetStates(m.name).nonEmpty }

      // Combine the remaining specification modules with the plant modules
      val remainingModules = filteredSpecs ++ plantModules

      // check what states have changed in transitions `t`
      val changedStates = t.target.states.keySet.filter(k => t.source.states(k) != t.target.states(k))

      // update the transition with the target states of the specifications
      val trans = StateMapTransition(t.source, StateMap(states=t.target.states, specs=specTargetStates.filter(_._2.nonEmpty).mapValues(_.get)), t.event)

      // filter out modules that should include this transitions, being those where
      // the state of the spec OR where at least one state variable is changed by the transition.
      val concernedModules = remainingModules.filter { m =>
        state.specs.getOrElse(m.name, "-1") != specTargetStates.getOrElse(m.name, None).getOrElse("") ||
          m.stateSet.states.intersect(changedStates).nonEmpty ||
          m.alphabet.events.contains(t.event)
      }

      var newStateFound = false
      for (m <- concernedModules) {
        if (plantModules.contains(m) || specTargetStates(m.name).nonEmpty) {
          val tReduced = getReducedStateMapTransition(trans, m)
          moduleTransitions(m.name) += tReduced
          if (!moduleStates(m.name).contains(tReduced.target)){
            moduleStates(m.name) += tReduced.target
            newStateFound = true
          }
        }
      }
      if (newStateFound) queue = trans.target :: queue
    }
  }
  println("DONE!")
  println(s"Number of iterations: $count")


  override def getAutomata: Automata = {

    val automatons = for {
      m <- modules
      nonLocalVariables =
        moduleTransitions(m.name).filter(_.event.getCommand == tau).flatMap(t => t.source.states.keys.filter(k => t.source.states(k) != t.target.states(k)))

      states: Map[StateMap, State] = moduleStates(m.name).map( s => {
        val state = StateMap(s.states.filterKeys(k => !nonLocalVariables.contains(k)), s.specs)
        val name = (
          (if (state.states.forall{ case (k,v) => initState.states(k) == v }
            && state.specs.forall{ case (k,v) => initState.specs(k) == v }) "INIT: " else "")
          + state.toString )
        (s, State(name))
      }).toMap
      transitions: Set[Transition] = moduleTransitions(m.name).filterNot(_.event.getCommand == tau).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
      alphabet: Alphabet = m.alphabet
      iState: State = states(getReducedStateMap(initState, m) )
      fStates: Option[Set[State]] =
        Some(states.filter(s => s._1.specs.forall(spec => specifications.isAccepting(spec._1,spec._2.toString))).values.toSet)
      forbiddenStates = moduleForbidden(m.name).map(states(_)) match {
        case x if x.nonEmpty => Some(x.toSet)
        case _ => None
      }
    } yield Automaton(m.name, states.values.toSet, alphabet, transitions, iState, fStates, forbiddenStates)
    Automata(automatons)

  }

}
