package modelbuilding.solvers

import modelbuilding.core._
import modelbuilding.core.modeling.{Model, ModularModel}
import modelbuilding.core.simulation.SUL
import modelbuilding.solvers.FrehagePlantBuilderWithPartialStates._

object FrehagePlantBuilderWithPartialStates {

  def getReducedStateMap(state: StateMap, model: ModularModel, module: String): StateMap =
    StateMap(state.name, state.states.filterKeys(s => model.stateMapping(module).states.contains(s)))

  def getReducedStateMapTransition(t: StateMapTransition, model: ModularModel, module: String): StateMapTransition =
    StateMapTransition(getReducedStateMap(t.source, model, module), getReducedStateMap(t.target, model, module), t.event)

}

class FrehagePlantBuilderWithPartialStates(_model: Model) extends BaseSolver {

  assert(_model.isModular, "modelbuilder.solver.FrehageSolverWithPartialStates requires a modular model.")
  //assert(_model.simulation.acceptsPartialStates, "modelbuilder.solver.FrehageSolverWithPartialStates requires a simulator that can evaluate partial states.")

  private val model = _model.asInstanceOf[ModularModel]
  private val simulator: SUL = model.simulation

  private var moduleQueue: Map[String, Set[StateMap]] = model.modules.map(_ -> Set(simulator.getInitState)).toMap
  private var moduleStates: Map[String, Set[StateMap]] = model.modules.map(_ -> Set.empty[StateMap]).toMap
  private var moduleTransitions: Map[String, Vector[StateMapTransition]] = model.modules.map(_ -> Vector.empty[StateMapTransition]).toMap

  for (m <- model.modules) {

    var queue: Vector[StateMap] = Vector(getReducedStateMap(simulator.getInitState, model, m))
    var states: Set[StateMap] = Set.empty[StateMap]
    var transitions: Vector[StateMapTransition] = Vector.empty[StateMapTransition]

    while (queue.nonEmpty) {

      states ++= queue

      val next = queue.flatMap(s => simulator.getOutgoingTransitions(s, model.eventMapping(m)))
      transitions ++= next.map( t => getReducedStateMapTransition(t, model, m))

      queue = next.map(s => s.target).filter(!states.contains(_))

    }

    moduleStates += (m -> states)
    moduleTransitions += (m -> transitions)

  }

  override def getAutomata: Automata = {

    val modules = for {
      m <- model.modules
      states: Map[StateMap, State] = moduleStates(m).map( s => {
        val name = (if ( s.states.forall{ case (k,v) => simulator.getInitState.states(k) == v } ) "INIT: " else "") + s.toString
        (s,State(name))
      }).toMap
      transitions: Set[Transition] = moduleTransitions(m).map( t => Transition(states(t.source), states(t.target), t.event)).toSet
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
