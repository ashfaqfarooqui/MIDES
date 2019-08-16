package modelbuilding.solvers

import SupremicaStuff.SupremicaHelpers
import grizzled.slf4j.Logging
import modelbuilding.algorithms.LStar.LStar
import modelbuilding.core.Automata
import modelbuilding.core.modelInterfaces.Model
import org.supremica._

class LStarPlantSolver(_model:Model) extends BaseSolver with SupremicaHelpers with Logging{


  val teacher = _model.simulation
  val runner = new LStar(teacher, wCEG).startLearning()



  override def getAutomata: Automata = {
    val sAut = createSupremicaAutomaton(runner.states,runner.transitions,runner.alphabet,runner.iState,runner.fState,runner.forbiddenStates)
    saveToXMLFile(iFilePath=s"./supremicaFiles/result_${_model.name}.xml",aut=new automata.Automata(sAut))
    Automata(Set(runner))
  }
}
