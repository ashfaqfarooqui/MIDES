package modelbuilding.solvers

import modelbuilding.core.{Automata, Automaton}
import modelbuilding.core.modelInterfaces.Model

class FrehageSolver(model: Model) extends BaseSolver {
  info("Starting to build the models using modelbuilding.solvers.FrehageSolver")
  override def getAutomata: Automata = Automata(Set.empty[Automaton])

}
