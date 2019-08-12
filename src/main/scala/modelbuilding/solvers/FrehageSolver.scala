package modelbuilding.solvers

import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.core.modelInterfaces.{Model, ModularModel, SUL}
import modelbuilding.core._
import modelbuilding.solvers.FrehageSolver._

object FrehageSolver {

  def getReducedStateMap(state: StateMap, model: ModularModel, module: Module): StateMap =
    StateMap(state.name, state.state.filterKeys(s => model.stateMapping(module).states.contains(s)))

  def getReducedStateMapTransition(t: StateMapTransition, model: ModularModel, module: Module): StateMapTransition =
    StateMapTransition(getReducedStateMap(t.source, model, module), getReducedStateMap(t.target, model, module), t.event)

}

class FrehageSolver(_model: Model) extends BaseSolver {

  assert(_model.isModular, "modelbuilder.solver.FrehageSolver requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]
  private val simulator: SUL = model.simulation

  info("Starting to build the models using modelbuilding.solvers.FrehageSolver")


  // One queue for each module to track new states that should be explored.
  private var globalStateSpace: Vector[StateMap] = Vector(simulator.getInitState)
  private var moduleExplored: Map[Module, Int] = model.modules.map(_ -> 0).toMap
  private var moduleStates: Map[Module, Vector[StateMap]] = model.modules.map(_ -> Vector.empty[StateMap]).toMap
  private var moduleTransitions: Map[Module, Set[StateMapTransition]] = model.modules.map(_ -> Set.empty[StateMapTransition]).toMap

  var count = 0
  // Loop until all modules are done exploring new states
  while (moduleExplored.values.exists(q => q < globalStateSpace.size)) {
//  for (i <- 1 to 20) {
    count += 1
    println("#############################")
    println(s"Iteration $count")
    println("* Global state space: " + globalStateSpace.size)
    println("* States: " + moduleStates.map(_._2.size).sum)
    println("* Transitions: " + moduleTransitions.map(_._2.size).sum)

    // Iterate over the modules to process them individually
    for (m <- model.modules) {

      println(s"Processing module $m")

      val stateIndex = (moduleExplored(m) until globalStateSpace.size).filter(i => !(moduleStates(m) contains getReducedStateMap(globalStateSpace(i), model, m)) )
      val moduleQueue = stateIndex.map(i => (getReducedStateMap(globalStateSpace(i), model, m),i))
      moduleExplored += (m -> globalStateSpace.size)

      /*println("######################### ")
      println("##### ", m)
//      println("##### ", moduleExplored(m))
      println("##### ", moduleQueue)
//      println("##### ", globalStateSpace.drop(moduleExplored(m)))
      println("##### ", moduleStates(m))*/
      moduleStates += (m -> (moduleStates(m) ++ moduleQueue.map(_._1).distinct))

      val next = moduleQueue.flatMap(s => simulator.getOutgoingTransitions(globalStateSpace(s._2), model.eventMapping(m)))
      moduleTransitions += (m -> (moduleTransitions(m) ++ next.map( t => getReducedStateMapTransition(t, model, m))))
      globalStateSpace ++= next.map(_.target)


    }

  }

  override def getAutomata: Automata = {

    val modules = for {
      m <- model.modules
      states: Map[StateMap, State] = moduleStates(m).map( s => {
        val name = (if ( s.state.forall{ case (k,v) => simulator.getInitState.state(k) == v } ) "INIT: " else "") + s.toString
        (s,State(name))
      }).toMap
      transitions: Set[Transition] = moduleTransitions(m).map( t => Transition(states(t.source), states(t.target), t.event))
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
