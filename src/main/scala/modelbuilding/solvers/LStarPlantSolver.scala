package modelbuilding.solvers

import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.Wmethod
import modelbuilding.algorithms.LStar.LStar
import modelbuilding.core.Automata
import modelbuilding.core.modeling.Model
import supremicastuff.SupremicaHelpers._
import org.supremica._

class LStarPlantSolver(_model:Model) extends BaseSolver with Logging{


  val teacher = _model.simulation

  val runner = new LStar(teacher,_model.alphabet, Wmethod(_model.alphabet,50)).startLearning()



  override def getAutomata: Automata = {
    val sAut = createSupremicaAutomaton(runner.states,runner.transitions,runner.alphabet,runner.iState,runner.fState,runner.forbiddenStates)
    saveToXMLFile(iFilePath=s"./supremicaFiles/result_${_model.name}.xml",aut=new automata.Automata(sAut))
    Automata(Set(runner))
  }
}
