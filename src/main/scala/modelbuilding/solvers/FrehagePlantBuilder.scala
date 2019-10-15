/*

Second version without partial states.

A brute force BFS that split the result into modules rather than a monolithic plant.

 */

package modelbuilding.solvers

import modelbuilding.core.{SUL, _}
import modelbuilding.core.modeling.{Model, ModularModel}
import modelbuilding.solvers.FrehagePlantBuilder._

import scala.collection.mutable

object FrehagePlantBuilder {

  def getReducedStateMap(state: StateMap, model: ModularModel, module: String): StateMap =
    StateMap(
      state.name,
      state.states.filterKeys(s => model.stateMapping(module).states.contains(s))
    )

  def getReducedStateMapTransition(
      t: StateMapTransition,
      model: ModularModel,
      module: String
    ): StateMapTransition =
    StateMapTransition(
      getReducedStateMap(t.source, model, module),
      getReducedStateMap(t.target, model, module),
      if (model.eventMapping(module).events contains t.event) t.event else Symbol(tau)
    )

}

class FrehagePlantBuilder(_sul: SUL) extends BaseSolver {

  val _model = _sul.model
  assert(_model.isModular, "modelbuilder.solver.FrehageSolver requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]

  private var queue: List[StateMap] = List(_sul.getInitState)

  // One queue for each module to track new states that should be explored.
  private val moduleStates: Map[String, mutable.Set[StateMap]] = model.modules
    .map(m => m -> mutable.Set(getReducedStateMap(_sul.getInitState, model, m)))
    .toMap
  private val moduleTransitions: Map[String, mutable.Set[StateMapTransition]] =
    model.modules.map(_ -> mutable.Set.empty[StateMapTransition]).toMap

  var count = 0
  // Loop until all modules are done exploring new states
  while ({
    count += 1
    println(s"Iteration: $count")
    queue.nonEmpty
  }) {

    val next: List[StateMapTransition] =
      queue.flatMap(_sul.getOutgoingTransitions(_, model.alphabet))

    queue = List.empty[StateMap]

    next.foreach { t =>
      val changedVars =
        t.target.states.keySet.filter(k => t.source.states(k) != t.target.states(k))
      var newStateFound = false
      for (m <- model.modules
           if (model.stateMapping(m).states intersect changedVars).nonEmpty) {
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

  override def getAutomata: Automata = {
    val modules = for {
      m <- model.modules
      nonLocalVariables = moduleTransitions(m)
        .filter(_.event.getCommand == tau)
        .flatMap(
          t => t.source.states.keys.filter(k => t.source.states(k) != t.target.states(k))
        )
      //      nonLocalVariables = Set.empty[String]
      states: Map[StateMap, State] = moduleStates(m)
        .map(s => {
          val state = StateMap(s.states.filterKeys(k => !nonLocalVariables.contains(k)))
          val name = (if (state.states
                            .forall { case (k, v) => _sul.getInitState.states(k) == v })
                        "INIT: "
                      else "") + state.toString
          (getReducedStateMap(s, model, m), State(name))
        })
        .toMap
      transitions: Set[Transition] = moduleTransitions(m)
        .filterNot(_.event.getCommand == tau)
        .map(getReducedStateMapTransition(_, model, m))
        .map(t => Transition(states(t.source), states(t.target), t.event))
        .toSet[Transition]
      //      transitions: Set[Transition] = moduleTransitions(m).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
      alphabet: Alphabet = model.eventMapping(m)
      iState: State      = states(getReducedStateMap(_sul.getInitState, model, m))
      fState: Option[Set[State]] = _sul.getGoalStates match {
        case Some(gs) => Some(gs.map(s => states(getReducedStateMap(s, model, m))))
        case None     => None
      }
    } yield Automaton(m, states.values.toSet, alphabet, transitions, iState, fState)
    Automata(modules)
  }

}
