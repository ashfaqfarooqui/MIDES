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
  [ ] Simulation: fix the duration of the actions.
  [ ] ...

  */

object FrehageCompositionalOptimizationBACKUP {

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
class FrehageCompositionalOptimizationBACKUP(_sul: SUL) extends BaseSolver {

  val _model = _sul.model
  assert(_model.isModular, "modelbuilder.solver.FrehageCompositionalOptimization requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]

  val _sim = _sul.simulator
  assert(_sim.isInstanceOf[TimedCodeSimulator], "modelbuilder.solver.FrehageCompositionalOptimization requires a simulator of type TimedCodeSimulator.")
  private val sim = _sim.asInstanceOf[TimedCodeSimulator]


  private var queue = collection.mutable.PriorityQueue((0, sim.initState))(
    Ordering.by((_: (Int, StateMap))._1).reverse
  )



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
    moduleStates.foreach(x => println(x._1 + x._2.map(s => (s.getKey("r_1_l").get, s.getKey("r_2_l").get))))
    nonLocalVariables foreach println
    queue.nonEmpty // && count < 10
  }) {
    val state = queue.dequeue()._2
    val next: List[StateMapTransition] =
      _sul.getOutgoingTransitions(state, model.alphabet)

    next.foreach { t =>

      val changedVars =
        t.target.states.keySet.filter(k => t.source.states(k) != t.target.states(k))
      var newStateFound = false
      for (m <- model.modules
           if (model.stateMapping(m).states intersect changedVars).nonEmpty) {

        val tReducedTarget = getReducedStateMap(t.target, model, m)

        if (model.eventMapping(m).events contains t.event)
          moduleTransitions(m) += StateMapTransition(
            getReducedStateMap(t.source, model, m),
            tReducedTarget,
            t.event
          )
        else nonLocalVariables(m) ++= (model.stateMapping(m).states intersect changedVars)

        if (!(moduleStates(m) contains tReducedTarget)) {
          moduleStates(m) += tReducedTarget
          newStateFound = true
        }
      }
      if (newStateFound) queue.enqueue((count,t.target))
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
        .map(t => StateMapTransition(
          getReducedStateMap(t.source, model, m),
          getReducedStateMap(t.target, model, m),
          Symbol(EventC(f"(${t.event.toString}, ${sim.calculateDuration(t)}%.2f)"))
        ))
        .map(t => Transition(states(t.source), states(t.target), t.event))
        .toSet[Transition]
      //      transitions: Set[Transition] = moduleTransitions(m).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
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
          Symbol(EventC(f"(${t.event.toString}, ${sim.calculateDuration(t)}%.2f)"))
        ))
        .map(t => Transition(states(t.source), states(t.target), t.event))
        .toSet[Transition]
      //      transitions: Set[Transition] = moduleTransitions(m).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
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
