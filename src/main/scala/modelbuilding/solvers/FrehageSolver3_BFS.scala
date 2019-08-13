/*

Second version without partial states.

A brute force BFS that split the result into modules rather than a monolithic plant.

*/

package modelbuilding.solvers


import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.core.modelInterfaces.{Model, ModularModel, SUL}
import modelbuilding.core._
import modelbuilding.solvers.FrehageSolver3_BFS._
import Helpers.Diagnostic._

import scala.collection.mutable

object FrehageSolver3_BFS {

  def getReducedStateMap(state: StateMap, model: ModularModel, module: Module): StateMap =
    StateMap(state.name, state.state.filterKeys(s => model.stateMapping(module).states.contains(s)))

  def getReducedStateMapTransition(t: StateMapTransition, model: ModularModel, module: Module): StateMapTransition =
    StateMapTransition(getReducedStateMap(t.source, model, module), getReducedStateMap(t.target, model, module), if (model.eventMapping(module).a contains t.event) t.event else Symbol(tau))

}

class FrehageSolver3_BFS(_model: Model) extends BaseSolver {

  assert(_model.isModular, "modelbuilder.solver.FrehageSolver requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]
  private val simulator: SUL = model.simulation

  private var queue: List[StateMap] = List(simulator.getInitState)

  // One queue for each module to track new states that should be explored.
  private val moduleStates: Map[Module, mutable.Set[StateMap]] = model.modules.map(m => m -> mutable.Set(getReducedStateMap(simulator.getInitState,model,m))).toMap
  private val moduleTransitions: Map[Module, mutable.Set[StateMapTransition]] = model.modules.map(_ -> mutable.Set.empty[StateMapTransition]).toMap

  var count = 0
  // Loop until all modules are done exploring new states
  while ( {
    count += 1
    println(s"Iterations: $count, States: ${queue.size}")
    time { queue.nonEmpty }
  }) {

    val next: List[StateMapTransition] = time { queue.flatMap(simulator.getOutgoingTransitions(_, model.alphabet)) }

    queue = List.empty[StateMap]

    time {
      next.foreach { t =>

 //       if (t.source.state.getOrElse("v2p", false) == "i3") println(s"###### ${t.source.state.getOrElse("i3", false)}")

        val changedVars = t.target.state.keySet.filter(k => t.source.state(k) != t.target.state(k))

        var newStateFound = false
        for (m <- model.modules if (model.stateMapping(m).states intersect changedVars).nonEmpty) {
          val tReduced = getReducedStateMapTransition(t, model, m)
          moduleTransitions(m) += tReduced
          if (!(moduleStates(m) contains tReduced.target)) {
            moduleStates(m) += tReduced.target
            newStateFound = true
          }
        }
        if (newStateFound) queue = t.target :: queue
      }
    }

  }

  override def getAutomata: Automata = {

    val modules = for {
      m <- model.modules
      nonLocalVariables = moduleTransitions(m).filter(_.event.getCommand == tau).flatMap(t => t.source.state.keys.filter(k => t.source.state(k) != t.target.state(k)))
//      nonLocalVariables = Set.empty[String]
      states: Map[StateMap, State] = moduleStates(m).map( s => {
        val state = StateMap(s.state.filterKeys(k => !nonLocalVariables.contains(k)))
        val name = (if ( state.state.forall{ case (k,v) => simulator.getInitState.state(k) == v } ) "INIT: " else "") + state.toString
        (getReducedStateMap(s,model,m),State(name))
      }).toMap
      transitions: Set[Transition] = moduleTransitions(m).filterNot(_.event.getCommand == tau).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
//      transitions: Set[Transition] = moduleTransitions(m).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
      alphabet: Alphabet = model.eventMapping(m)
      iState: State = states(getReducedStateMap(simulator.getInitState, model, m) )
      fState: Option[Set[State]] = simulator.getGoalStates match {
        case Some(gs) => Some(gs.map( s => states(getReducedStateMap(s, model, m)) ))
        case None => None
      }
    } yield Automaton(m, states.values.toSet, alphabet, transitions, iState, fState)

    Automata(modules)
  }

}
