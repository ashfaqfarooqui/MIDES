/*

First version that work with simulations that does not allow partial states. It is inefficient
due to the large number of states that have to be expanded in each module.

*/

package modelbuilding.solvers


import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.core.modelInterfaces.{Model, ModularModel, SUL}
import modelbuilding.core._
import modelbuilding.solvers.FrehageSolverWithoutPartialStates._
import Helpers.Diagnostic._

import scala.collection.mutable

object FrehageSolverWithoutPartialStates {

  def getReducedStateMap(state: StateMap, model: ModularModel, module: Module): StateMap =
    StateMap(state.name, state.state.filterKeys(s => model.stateMapping(module).states.contains(s)))

  def getReducedStateMapTransition(t: StateMapTransition, model: ModularModel, module: Module): StateMapTransition =
    StateMapTransition(getReducedStateMap(t.source, model, module), getReducedStateMap(t.target, model, module), t.event)

}

class FrehageSolverWithoutPartialStates(_model: Model) extends BaseSolver {

  assert(_model.isModular, "modelbuilder.solver.FrehageSolver requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]
  private val simulator: SUL = model.simulation

  // One queue for each module to track new states that should be explored.
  private val moduleStates: Map[Module, mutable.Map[StateMap,Boolean]] = model.modules.map(_ -> mutable.Map(simulator.getInitState -> false)).toMap
  private val moduleTransitions: Map[Module, mutable.MutableList[StateMapTransition]] = model.modules.map(_ -> mutable.MutableList.empty[StateMapTransition]).toMap

  var count = 0
  // Loop until all modules are done exploring new states
  while ( moduleStates.exists( _._2.exists(_._2 == false))  ) {
    //  for (i <- 1 to 20) {
    count += 1
    println("#############################")
    println("#############################")
    println("#############################")
    println("#############################")
    println("#############################")
    println("#############################")
    println("#############################")
    println(s"Iteration $count")
    println("* States: " + moduleStates.map(_._2.size).sum)
    println("* Transitions: " + moduleTransitions.map(_._2.size).sum)

    // Iterate over the modules to process them individually
    for (m <- model.modules) {

      println(s"Processing module $m")

      val moduleQueue = time { moduleStates(m).filter(_._2 == false).keys }

      time {  moduleQueue.foreach(s => moduleStates(m)(s) = true) }

      val next = time { moduleQueue.flatMap(s => simulator.getOutgoingTransitions(s, model.eventMapping(m))) }
      time { moduleTransitions(m) ++= next }
      time {
//        val stateChanges: Map[StateMap, Set[String]] =
//          next.map(t => (t.target, t.target.state.keys.filter(k => t.target.state(k) != t.source.state(k)).toSet)).toMap

        for (m <- model.modules) {
//          next.filter( t => !(moduleStates(m) contains t.target) ).foreach( t =>  moduleStates(m) += (t.target -> false))
          next.foreach( t => if (!(moduleStates(m) contains t.target)) moduleStates(m) += (t.target -> false))
        }
      }
    }

  }

  override def getAutomata: Automata = {

    val modules = for {
      m <- model.modules
      states: Map[StateMap, State] = moduleStates(m).keys.map( s => {
        val name = (if ( s.state.forall{ case (k,v) => simulator.getInitState.state(k) == v } ) "INIT: " else "") + getReducedStateMap(s,model,m).toString
        (getReducedStateMap(s,model,m),State(name))
      }).toMap
      transitions: Set[Transition] = moduleTransitions(m).map(getReducedStateMapTransition(_,model,m)).map( t => Transition(states(t.source), states(t.target), t.event)).toSet[Transition]
      alphabet: Alphabet = model.eventMapping(m)
      iState: State = states(getReducedStateMap(simulator.getInitState, model, m) )
      fState: Option[Set[State]] = simulator.getGoalStates match {
        case Some(gs) => Some(gs.map( s => states(getReducedStateMap(s, model, m)) ))
        case None => None
      }
    } yield Automaton(m, states.values.toSet, alphabet, transitions.toSet, iState, fState)

    Automata(modules)
  }

}
