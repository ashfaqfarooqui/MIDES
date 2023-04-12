package Mides

import grizzled.slf4j.Logging
import modelbuilding.core.SUL
import modelbuilding.helpers.ConfigHelper
import supremicastuff.SupremicaHelpers
import modelbuilding.solvers._

object Mides extends Logging {

  val modelName: String = ConfigHelper.model  //"MachineBufferNoSpec"
  val solver: String    = ConfigHelper.solver //"LStarPlantLearner" // "modular", "mono"

  val sul: SUL = modelName match {

    case "CatMouse" =>
      SUL(
        CatAndMouse.CatAndMouse,
        new CatAndMouse.SimulateCatAndMouse,
        None,
        false
      )
    case "CatMouseModular" =>
      SUL(
        CatAndMouseModular.CatAndMouseModular,
        new CatAndMouseModular.SimulateCatAndMouseModular,
        Some(CatAndMouseModular.CatAndMouseModularSpecification()),
        false
      )
    case "MachineBuffer" =>
      SUL(
        MachineBuffer.MachineBuffer,
        new MachineBuffer.SimulateMachineBuffer,
        Some(MachineBuffer.MachineBufferSpecifications()),
        false
      )
    case "MachineBufferWithControl" =>
      SUL(
        MachineBuffer.MachineBufferWithControl,
        new MachineBuffer.SimulateMachineBufferWithControl,
        Some(MachineBuffer.MachineBufferSpecifications()),
        false
      )
    case "MachineBufferNoSpec" =>
      SUL(
        MachineBuffer.MachineBuffer,
        new MachineBuffer.SimulateMachineBuffer,
        None,
        true
      )
    case "RoboticArm" =>
      SUL(RobotArm.Arm, new RobotArm.SimulateArm(3, 3), None, false)
    case "Sticks" =>
      SUL(StickPicking.Sticks, new StickPicking.SimulateSticks(5), None, false)
    case "AGV" =>
      SUL(AGV.Agv, new AGV.SimulateAgv, Some(AGV.AGVSpecifications()), false)

    case _ => throw new Exception("A model wasn't defined.")
  }

  def main(args: Array[String]): Unit = {

    //info(s"Running sul: $sul")
    info(s"Starting learner for : $modelName, using $solver as solver")

    val result = solver match {
      case "ModularPlantLearnerWithPartialStates" =>
        new FrehagePlantBuilderWithPartialStates(sul)
      case "ModularPlantLearner"      => new FrehagePlantBuilder(sul)
      case "ModularSupervisorLearner" => new FrehageModularSupSynthesis(sul)
      case "MonolithicPlantSolver"    => new MonolithicSolver(sul)
      case "MonolithicSupSolver"      => new MonolithicSupSolver(sul)
      case "ModularSupSolver"         => new ModularSupSolver(sul)
      case "LStarPlantLearner"        => new LStarPlantSolver(sul)
      case "LStarSupervisorLearner"   => new LStarSuprSolver(sul)

    }

    info("Learning done!, writing results")

    val automata = result.getAutomata

    automata.modules foreach println
    automata.modules.foreach(_.createDotFile)
    SupremicaHelpers.exportAsSupremicaAutomata(automata, name = modelName)
  }

}
