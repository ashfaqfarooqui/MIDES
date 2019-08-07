package modelbuilding.solvers

import modelbuilding.core.modelInterfaces.Model
import SupremicaStuff.SupremicaHelpers
import net.sourceforge.waters.subject.module.ModuleSubject
import org.supremica.automata.Automata

class MonolithicSolver(model:Model) extends BaseSolver with SupremicaHelpers{
  override def getAutomata: Automata = ???

  override val mModule: ModuleSubject = moduleFactory("")


}
