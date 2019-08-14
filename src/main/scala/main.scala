
import grizzled.slf4j.Logging
import modelbuilding.core.modelInterfaces._
import modelbuilding.models._


import modelbuilding.solvers.{FrehageSolverWithPartialStates, ModularSupSolver, MonolithicSolver, MonolithicSupSolver}

object ModelBuilder extends Logging {

  val modelName = "CatMouseModular"

  val model: Model = modelName match{
    case "TestUnit" => TestUnit.TransferLine
    case "CatMouse" => CatAndMouse.CatAndMouse
    case "CatMouseModular" => CatAndMouseModular.CatAndMouseModular
    case "MachineBuffer" => MachineBuffer.MachineBuffer
    case "RoboticArm" => RobotArm.Arm
    case "Sticks" => StickPicking.Sticks
    case "AGV" => AGV.Agv
    case _ => throw new Exception("A model wasn't defined.")

  }
  val solver: String = "modularSupSolver"

  def main(args: Array[String]) : Unit= {

    info(s"Running model: $model")

    val result = solver match {
      case "frehage" => new FrehageSolverWithPartialStates(model)
      case "monolithic" => new MonolithicSolver(model)
      case "monolithicSupSolver" => new MonolithicSupSolver(model)
      case "modularSupSolver" => new ModularSupSolver(model)

    }

//    info("Learning done!")

    val automata = result.getAutomata

    automata.modules foreach println
    automata.modules.foreach(_.createDotFile)

  }

}
