import grizzled.slf4j.Logging
import modelbuilding.core.SUL
import modelbuilding.helpers.ConfigHelper
import modelbuilding.models.TestUnit.TLSpecifications
import modelbuilding.models.ZenuityLaneChange.LaneChangeMoreInputs.LaneChangeSimulateMonolithic
import modelbuilding.models._
import modelbuilding.solvers._
/*
 * Learning Automata for Supervisory Synthesis
 *  Copyright (C) 2019
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import supremicastuff.SupremicaHelpers

object ModelBuilder extends Logging {

  val modelName      = ConfigHelper.model  //"MachineBufferNoSpec"
  val solver: String = ConfigHelper.solver //"LStarPlantLearner" // "modular", "mono"

  val sul: SUL = modelName match {
    case "TestUnit" =>
      SUL(
        TestUnit.TransferLine,
        new TestUnit.SimulateTL,
        Some(TLSpecifications()),
        true
      )
    case "TestUnitOPC" =>
      SUL(
        TestUnit.TransferLine,
        new TestUnit.TLOPCSimulate,
        Some(TLSpecifications()),
        true
      )
    case "TestUnitNoSpec" =>
      SUL(
        TestUnit.TransferLine,
        new TestUnit.SimulateTL,
        None,
        true
      )
    case "TestUnitNoSpecOPC" =>
      SUL(
        TestUnit.TransferLine,
        new TestUnit.TLOPCSimulate,
        None,
        true
      )
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

    case "MachineBufferOPC" =>
      SUL(
        MachineBuffer.MachineBuffer,
        new MachineBuffer.MBOPCSimulate,
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
    case "MachineBufferNoSpecOPC" =>
      SUL(
        MachineBuffer.MachineBuffer,
        new MachineBuffer.MBOPCSimulate,
        None,
        true
      )
    case "RoboticArm" =>
      SUL(RobotArm.Arm, new RobotArm.SimulateArm(3, 3), None, false)
    case "Sticks" =>
      SUL(StickPicking.Sticks, new StickPicking.SimulateSticks(5), None, false)
    case "AGV" =>
      SUL(AGV.Agv, new AGV.SimulateAgv, Some(AGV.AGVSpecifications()), false)
    case "LaneChange" =>
      SUL(
        ZenuityLaneChange.LaneChange,
        new ZenuityLaneChange.LaneChangeSimulate,
        None,
        false
      )
    case "LaneChangeMonolithic" =>
      SUL(
        ZenuityLaneChange.LaneChangeMoreInputs.LaneChange,
        new LaneChangeSimulateMonolithic,
        None,
        false
      )
    case _ => throw new Exception("A model wasn't defined.")
  }

  def main(args: Array[String]): Unit = {

    //info(s"Running sul: $sul")
    info(s"Starting learner for : $modelName, using $solver as solver")

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

    info("Learning done!, writing results")

    val automata = result.getAutomata

    automata.modules foreach println
    automata.modules.foreach(_.createDotFile)
    SupremicaHelpers.exportAsSupremicaAutomata(automata, name = modelName)
  }

}
