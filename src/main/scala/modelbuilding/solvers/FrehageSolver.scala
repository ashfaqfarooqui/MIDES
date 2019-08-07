package modelbuilding.solvers

import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.core.modelInterfaces.{Model, ModularModel}
import modelbuilding.core.{Automata, Automaton, StateMap}
import modelbuilding.solvers.FrehageSolver._

object FrehageSolver {
  def getReducedStateMap(state: StateMap, model: ModularModel, module: Module): StateMap =
    StateMap(state.name, state.state.filterKeys(s => model.stateMapping(module).states.contains(s)))
}

class FrehageSolver(_model: Model) extends BaseSolver {

  assert(_model.isModular, "modelbuilder.solver.FrehageSolver requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]
  private val sul= model.simulation
  private val sim = sul.simulator

  info("Starting to build the models using modelbuilding.solvers.FrehageSolver")


  // One queue for each module to track new states that should be explored.
  private var moduleQueue: Map[Module, Set[StateMap]] = model.modules.map(_ -> Set(sim.initState)).toMap
  private var moduleHistory: Map[Module, Set[StateMap]] = model.modules.map(_ -> Set.empty[StateMap]).toMap

  moduleQueue foreach println

  // Loop until all modules are done exploring new states
//  while (moduleQueue.values.exists(q => q.nonEmpty)) {
  for (i <- 1 to 5) {
    println("##########################")

    // Iterate over the modules to process them individually
    for (m <- model.modules) {

      println("##############")
      println(s"Evaluating module `$m`")
      moduleQueue += (m -> moduleQueue(m).filter(s => !moduleHistory(m).contains(getReducedStateMap(s, model, m))))
      println(s"Queue: ${moduleQueue(m)}")
      moduleQueue(m).foreach(s => println(s, getReducedStateMap(s, model, m), moduleHistory(m).contains(getReducedStateMap(s, model, m))))

      moduleHistory += (m -> (moduleHistory(m) ++ moduleQueue(m).map(s => getReducedStateMap(s, model, m))))
      println(s"History: ${moduleHistory(m)}")

      val next = moduleQueue(m).flatMap(s => sul.getNextState(s, model.eventMapping(m)))
      println(s"Next: $next")

      moduleQueue(m).empty
      for (m <- model.modules) moduleQueue += (m -> (moduleQueue(m) ++ next.map(s => s.target)))

    }

  }

  println("##############")
  moduleHistory.foreach{case (m, q) => println(s"$m states: {${q.map(s => s"(${s.state.mkString(",")})").mkString(",")}}")}


  override def getAutomata: Automata = Automata(Set.empty[Automaton])

}
