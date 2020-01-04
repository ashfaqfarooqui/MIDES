package modelbuilding.solvers

import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.Wmethod
import modelbuilding.algorithms.LStar.LStar
import modelbuilding.core.{Automata, _}

/**
 * The class that is repsonsible to start the [[LStar]] algorithm in order to learn a plant model.
 *
 * @param _sul must be a monolithic model.
 * 
 */
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
