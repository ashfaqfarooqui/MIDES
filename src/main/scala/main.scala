import grizzled.slf4j.Logging
import modelbuilding.core.{LearningType, SUL}
import modelbuilding.core.modeling.Model
import modelbuilding.models.TestUnit.TLSpecifications
import modelbuilding.models._
import modelbuilding.solvers._
import supremicastuff.SupremicaHelpers
import supremicastuff.SupremicaHelpers._

object ModelBuilder extends Logging {

  val supervisor = LearningType.SUPERVISOR
  val plant      = LearningType.PLANT

  val modelName = "MachineBuffer"
  info(s"Starting with mode : $modelName")

  val sul: SUL = modelName match {
    case "TestUnit" =>
      SUL(
        TestUnit.TransferLine,
        new TestUnit.SimulateTL,
        Some(TLSpecifications()),
        supervisor,
        true
      )
    case "CatMouse" =>
      SUL(
        CatAndMouse.CatAndMouse,
        new CatAndMouse.SimulateCatAndMouse,
        None,
        plant,
        false
      )
    case "CatMouseModular" =>
      SUL(
        CatAndMouseModular.CatAndMouseModular,
        new CatAndMouseModular.SimulateCatAndMouseModular,
        Some(CatAndMouseModular.CatAndMouseModularSpecification()),
        supervisor,
        false
      )
    case "MachineBuffer" =>
      SUL(
        MachineBuffer.MachineBuffer,
        new MachineBuffer.SimulateMachineBuffer,
        Some(MachineBuffer.MachineBufferSpecifications()),
        supervisor,
        false
      )
    case "RoboticArm" =>
      SUL(RobotArm.Arm, new RobotArm.SimulateArm(3, 3), None, plant, false)
    case "Sticks" =>
      SUL(StickPicking.Sticks, new StickPicking.SimulateSticks(5), None, plant, false)
    case "AGV" =>
      SUL(AGV.Agv, new AGV.SimulateAgv, Some(AGV.AGVSpecifications()), supervisor, false)
    case "LaneChange" =>
      SUL(
        ZenuityLaneChange.LaneChange,
        ZenuityLaneChange.LaneChangeSimulate(),
        None,
        plant,
        false
      )
    case _ => throw new Exception("A model wasn't defined.")
  }

  val solver: String = "LStarPlantLearner" // "modular", "mono"

  def main(args: Array[String]): Unit = {

    info(s"Running sul: $sul")

    val result = solver match {
      case "frehage1"              => new FrehagePlantBuilderWithPartialStates(sul)
      case "frehage2"              => new FrehagePlantBuilder(sul)
      case "frehage3"              => new FrehageModularSupSynthesis(sul)
      case "monolithicPlantSolver" => new MonolithicSolver(sul)
      case "monolithicSupSolver"   => new MonolithicSupSolver(sul)
      case "modularSupSolver"      => new ModularSupSolver(sul)
      case "LStarPlantLearner"     => new LStarPlantSolver(sul)
      case "LStarSuprLearner"      => new LStarSuprSolver(sul)

    }

    info("Learning done!")

    val automata = result.getAutomata

    automata.modules foreach println
    automata.modules.foreach(_.createDotFile)
    SupremicaHelpers.exportAsSupremicaAutomata(automata, name = modelName)
  }

}
