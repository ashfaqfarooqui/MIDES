package modelbuilding.solvers

import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.Wmethod
import modelbuilding.algorithms.LStar.LStar
import modelbuilding.core.{Alphabet, Automata, SUL, Symbol, tau}
import modelbuilding.core.modeling.Model

class LStarPlantSolver(_sul: SUL) extends BaseSolver with Logging {

  val _model   = _sul.model
  val teacher  = _sul
  val alphabet = _model.alphabet + Alphabet(Symbol(tau))
  val runner = new LStar(teacher, None, alphabet, Wmethod(alphabet, 150))
    .startLearning()
    .removeTauAndDump

  override def getAutomata: Automata = {
    Automata(Set(runner))
  }
}
