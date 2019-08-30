package modelbuilding.solvers

import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.Wmethod
import modelbuilding.algorithms.LStar.LStar
import modelbuilding.core.modeling.{Model, Specifications}
import modelbuilding.core.{Alphabet, Automata, SUL, Symbol, tau}

class LStarSuprSolver(_sul:SUL) extends BaseSolver with Logging {


  assert(_sul.specification.isDefined, "Specs need to be defined")

  val model = _sul.model
  val specs = _sul.specification.get.getSupremicaSpecs
  assert(specs.nonEmpty, "Specification automaton must exist")


  val teacher = _sul
  val alphabet = model.alphabet + Alphabet(Symbol(tau))
  val runner = new LStar(teacher,alphabet, Wmethod(alphabet,50)).startLearning()



  override def getAutomata: Automata = {
    Automata(Set(runner))
  }
}
