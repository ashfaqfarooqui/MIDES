
import grizzled.slf4j.Logging
import modelbuilding.core.modelInterfaces._
import modelbuilding.models._
import modelbuilding.solvers.{FrehageSolverWithPartialStates, MonolithicSolver}

object ModelBuilder extends Logging {

//  val model: Model = CatAndMouse.CatAndMouseModular
//  val model: Model = CatAndMouseModular.CatAndMouseModular
// val model: Model = MachineBuffer.MachineBuffer
  val model: Model = AGV.Agv

  val solver: String = "frehage" // "modular", "mono"

  def main(args: Array[String]) : Unit= {

//    info("Automata learn!")

    val result = solver match {
      case "frehage" => new FrehageSolverWithPartialStates(model)
      case "monolithic" => new MonolithicSolver(model)
    }

//    info("Learning done!")

    val automata = result.getAutomata

    automata.modules foreach println
    automata.modules.foreach(_.createDotFile)

  }

}
