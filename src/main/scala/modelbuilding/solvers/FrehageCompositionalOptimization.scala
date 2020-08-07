/*

Second version without partial states.

A brute force BFS that split the result into modules rather than a monolithic plant.

 */

package modelbuilding.solvers

import modelbuilding.core
import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core.interfaces.simulator.TimedCodeSimulator
import modelbuilding.core.{SUL, _}
import modelbuilding.solvers.FrehageCompositionalOptimization._

import scala.collection.mutable

/*

# Combining Learning with Compositional Optimiztaion


## Contributions:
  - Can reduce the memory allocation of the learning based when pruning non optimal paths

## Challanges:
  - Direct the learning based on the optimization
    - Identify "target states" and "source states" for the optimization
  - Separate the search on local and shared events
  - DFS on local events in source states of each module
  - BFS on shared events in all combination of local states to determine if they are target states and to identify new source states

TODO:
  [x] Simulation: fix the duration of the actions.
  ## Ideas
  [ ] Store the interesting alphabet with each new state. That way the expansion only needs to check those events that CAN add new value.
    i.e interesting evenst are: alphabet\(union of all events in modules that did not have any variable change)
    EDIT: Replace the queue of states with a queue of transitions.
  [ ] Replace for (m <- model.modules) model.modules with "potentially interesting modules", OR CAN WE REALLY?


  
  */

object FrehageCompositionalOptimization {

  def getReducedStateMap(state: StateMap, model: ModularModel, module: String): StateMap =
    StateMap(
      state.name,
      state.states.filterKeys(s => model.stateMapping(module).states.contains(s))
    )
}

/**
 * The implementation of the ... as defined in the paper
 * "..."
 * Hagebring et. al. ...
 *
 * @param _sul must be a modular model.
 */
class FrehageCompositionalOptimization(_sul: SUL) extends BaseSolver {

  val _model = _sul.model
  assert(_model.isModular, "modelbuilder.solver.FrehageCompositionalOptimization requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]

  private val _sim = _sul.simulator
  assert(_sim.isInstanceOf[TimedCodeSimulator], "modelbuilder.solver.FrehageCompositionalOptimization requires a simulator of type TimedCodeSimulator.")
  private val sim = _sim.asInstanceOf[TimedCodeSimulator]


  private val sources = collection.mutable.PriorityQueue((0.0, _sul.getInitState))(
    Ordering.by((_: (Double, StateMap))._1).reverse
  )
  private val transitions = collection.mutable.PriorityQueue.empty[(Double,StateMapTransition)](
    Ordering.by((_: (Double, StateMapTransition))._1).reverse
  )



  // One queue for each module to track new states that should be explored.
  private val moduleStates: Map[String, mutable.Set[StateMap]] = model.modules
    .map(m => m -> mutable.Set(getReducedStateMap(_sul.getInitState, model, m)))
    .toMap
  private val moduleTransitions: Map[String, mutable.ListBuffer[StateMapTransition]] =
    model.modules.map(_ -> mutable.ListBuffer.empty[StateMapTransition]).toMap

  // Keep track of the state variables that should be kept for the final automaton
  private val nonLocalVariables: Map[String, mutable.Set[String]] =
    model.modules.map(m => m -> mutable.Set.empty[String]).toMap

  // Starting in the initial state, loop until all source states have been expanded
  var count = 0
  while ({
    count += 1
    println(s"Iteration: $count")
    sources.nonEmpty // && count < 10
  }) {
    val source = sources.dequeue()
    val state = source._2

    // Add the outgoing transitions from the source to the queue to start the expansion
    transitions ++= _sul.getOutgoingTransitions(state, model.alphabet).map(t => (sim.calculateDuration(t),t))

    // Loop until there are no new paths over local transitions to expand for the current source state.
    var countTrans = 0
    while ({
      countTrans += 1
      transitions.nonEmpty // && count < 10
    })
    {

      val (d, t) = transitions.dequeue()

      println(s"Trans: $countTrans")
//      println(s"Trans: $countTrans", s" ${t.event} ${t.source.getState("r_1_l")} ${t.source.getState("r_2_l")} $d ${sim.calculateDuration(t)}")

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
          if( !(moduleStates(m) contains tReducedTarget)) {
            moduleStates(m) += tReducedTarget
            newStateFound = true
          }
          if (!eventIsPartOfModule) nonLocalVariables(m) ++= changedLocal
          possibleCommands += model.eventMapping(m)
        }
      }

      if (newStateFound) transitions ++= _sul.getOutgoingTransitions(t.target, possibleCommands).map(t => (d + sim.calculateDuration(t),t))

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
      transitions: Set[Transition] = moduleTransitions(m).toList
        .map(t => Transition(
          states(getReducedStateMap(t.source, model, m)),
          states(getReducedStateMap(t.target, model, m)),
          Symbol(new ControllableCommand(f"(${t.event.toString}, ${sim.calculateDuration(t)}%.2f)"))
        )).toSet[Transition]
      alphabet: Alphabet = Alphabet(transitions.map(_.event))
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
        .map(t => StateMapTransition(
          getReducedStateMap(t.source, model, m),
          getReducedStateMap(t.target, model, m),
          Symbol(new ControllableCommand(f"(${t.event.toString}, ${sim.calculateDuration(t)}%.2f)"))
        ))
        .map(t => Transition(states(t.source), states(t.target), t.event))
        .toSet[Transition]
      alphabet: Alphabet = Alphabet(transitions.map(_.event))
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
