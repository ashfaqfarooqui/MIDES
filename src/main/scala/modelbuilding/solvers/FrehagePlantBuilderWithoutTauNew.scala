/*

Second version without partial states.

A brute force BFS that split the result into modules rather than a monolithic plant.

 */

package modelbuilding.solvers

import modelbuilding.core
import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core.{SUL, _}
import modelbuilding.solvers.FrehagePlantBuilderWithoutTauNew._

import scala.collection.mutable

object FrehagePlantBuilderWithoutTauNew {

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
      t.event
    )
}

/**
 * The implementation of the Modular Supervisor Learner as defined in the paper
 * "Active Learning of Modular Plant Models"
 * Farooqui et. al. Wodes 2020
 *
 * @param _sul must be a modular model.
 */
class FrehagePlantBuilderWithoutTauNew(_sul: SUL) extends BaseSolver {

  val _model = _sul.model
  assert(_model.isModular, "modelbuilder.solver.FrehagePlantBuilderWithoutTau requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]

  private var queue: List[StateMapTransition] = _sul.getOutgoingTransitions(_sul.getInitState, model.alphabet)

  // One queue for each module to track new states that should be explored.
  private val moduleStates: Map[String, mutable.Set[StateMap]] = model.modules
    .map(m => m -> mutable.Set(getReducedStateMap(_sul.getInitState, model, m)))
    .toMap
  private val moduleTransitions: Map[String, mutable.Set[StateMapTransition]] =
    model.modules.map(_ -> mutable.Set.empty[StateMapTransition]).toMap

  // Keep track of the state variables that should be kept for the final automaton
  private val nonLocalVariables: Map[String, mutable.Set[String]] =
    model.modules.map(m => m -> mutable.Set.empty[String]).toMap

  var count = 0
  // Loop until all modules are done exploring new states
  while ({
    count += 1
    println(s"Iteration: $count")
    queue.nonEmpty
  }) {

    val next: List[StateMapTransition] = queue
    queue = List.empty[StateMapTransition]

    next.foreach { t =>
      val changedVars = t.target.states.keySet.filter(k => t.source.states(k) != t.target.states(k))
      var newStateFound = false
      var possibleCommands: Alphabet = Alphabet()
      for (m <- model.modules) {
        val changedLocal = model.stateMapping(m).states intersect changedVars
        val tReducedTarget = getReducedStateMap(t.target, model, m)
        val eventIsPartOfModule = model.eventMapping(m).events contains t.event
        if (eventIsPartOfModule) {
          moduleTransitions(m) += StateMapTransition(
            getReducedStateMap(t.source, model, m),
            tReducedTarget,
            t.event
          )
        }
        if (changedLocal.nonEmpty) {
          if (!eventIsPartOfModule) nonLocalVariables(m) ++= changedLocal
          if( !(moduleStates(m) contains tReducedTarget)) {
            moduleStates(m) += tReducedTarget
            newStateFound = true
          }
          possibleCommands += model.eventMapping(m)
        }
      }

      if (newStateFound) queue = _sul.getOutgoingTransitions(t.target, possibleCommands) ++ queue
    }

  }

  override def getAutomata: Automata = getAutomataPruned
  def getAutomataPruned: Automata = {
    val modules = for {
      m <- model.modules
      states: Map[StateMap, State] = moduleStates(m)
        .map(s => {
          val state = StateMap(s.states.filterKeys(k => !nonLocalVariables(m).contains(k)))
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
    core.Automata(modules)
  }

  def getAutomataFull: Automata = {
    val modules = for {
      m <- model.modules
      states: Map[StateMap, State] = moduleStates(m)
        .map(s => {
          val state = s
          val name = (if (state.states
                            .forall { case (k, v) => _sul.getInitState.states(k) == v })
                        "INIT: "
                      else "") + state.toString
          (getReducedStateMap(s, model, m), State(name))
        })
        .toMap
      transitions: Set[Transition] = moduleTransitions(m)
        .map(getReducedStateMapTransition(_, model, m))
        .map(t => Transition(states(t.source), states(t.target), t.event))
        .toSet[Transition]
      //      transitions: Set[Transition] = moduleTransitions(m).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
      alphabet: Alphabet = new Alphabet(model.eventMapping(m).events, true)
      iState: State      = states(getReducedStateMap(_sul.getInitState, model, m))
      fState: Option[Set[State]] = _sul.getGoalStates match {
        case Some(gs) => Some(gs.map(s => states(getReducedStateMap(s, model, m))))
        case None     => None
      }
    } yield core.Automaton(
      m,
      states.values.toSet,
      alphabet,
      transitions,
      iState,
      fState
    )
    core.Automata(modules)
  }

}
