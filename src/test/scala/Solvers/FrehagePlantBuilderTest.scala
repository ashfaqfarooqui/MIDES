package Solvers

import modelbuilding.core.{LearningType, SUL}
import modelbuilding.models.TestUnit.TLSpecifications
import modelbuilding.models.{AGV, CatAndMouse, CatAndMouseModular, MachineBuffer, RobotArm, StickPicking, TestUnit}
import modelbuilding.solvers.{FrehagePlantBuilder, MonolithicSolver}
import org.scalatest.FunSuite

class FrehagePlantBuilderTest extends FunSuite {


  test("FrehagePlantBuilderTest plant solver"){

    val supervisor = LearningType.SUPERVISOR
    val plant = LearningType.PLANT

    val testList = Map("MachineBuffer"->Map("size"->2,"tran"->8,"states"->4))
    val modelName = "MachineBuffer"
    testList.foreach { t =>
      val sul = t._1 match {
        case "TestUnit" => SUL(TestUnit.TransferLine, new TestUnit.SimulateTL, Some(TLSpecifications()), supervisor, true)
        case "CatMouse" => SUL(CatAndMouse.CatAndMouse, new CatAndMouse.SimulateCatAndMouse, None, plant, false)
        case "CatMouseModular" => SUL(CatAndMouseModular.CatAndMouseModular, new CatAndMouseModular.SimulateCatAndMouseModular, Some(CatAndMouseModular.CatAndMouseModularSpecification()), supervisor, false)
        case "MachineBuffer" => SUL(MachineBuffer.MachineBuffer, new MachineBuffer.SimulateMachineBuffer, Some(MachineBuffer.MachineBufferSpecifications()), supervisor, false)
        case "RoboticArm" => SUL(RobotArm.Arm, new RobotArm.SimulateArm(3, 3), None, plant, false)
        case "Sticks" => SUL(StickPicking.Sticks, new StickPicking.SimulateSticks(5), None, plant, false)
        case "AGV" => SUL(AGV.Agv, new AGV.SimulateAgv, Some(AGV.AGVSpecifications()), supervisor, false)
        case _ => throw new Exception("A model wasn't defined.")
      }

      val aut = new FrehagePlantBuilder(sul).getAutomata.modules
      assert(aut.size == t._2("size"))



    }
  }
}
