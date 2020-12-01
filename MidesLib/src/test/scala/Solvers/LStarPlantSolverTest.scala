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

package Solvers

import modelbuilding.core.SUL
import TestUnit.TLSpecifications
import modelbuilding.models._
import modelbuilding.solvers.LStarPlantSolver
import org.scalatest.FunSuite

class LStarPlantSolverTest extends FunSuite {

  test("lStar solver") {

    val testList = Map(
      "MachineBuffer" -> Map("size" -> 1, "tran" -> 25, "states" -> 5),
      "TestUnit"      -> Map("size" -> 1, "tran" -> 72, "states" -> 9)
    )
    testList.foreach { t =>
      val sul = t._1 match {
        case "TestUnit" =>
          SUL(
            TestUnit.TransferLine,
            new TestUnit.SimulateTL,
            Some(TLSpecifications()),
            true
          )
        case "CatMouse" =>
          SUL(CatAndMouse.CatAndMouse, new CatAndMouse.SimulateCatAndMouse, None, false)
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
        case "RoboticArm" =>
          SUL(RobotArm.Arm, new RobotArm.SimulateArm(3, 3), None, false)
        case "Sticks" =>
          SUL(StickPicking.Sticks, new StickPicking.SimulateSticks(5), None, false)
        case "AGV" =>
          SUL(
            AGV.Agv,
            new AGV.SimulateAgv,
            Some(AGV.AGVSpecifications()),
            false
          )
        case _ => throw new Exception("A model wasn't defined.")
      }

      val aut = new LStarPlantSolver(sul).getAutomata.modules
      assert(aut.size == t._2("size"))
      assert(aut.head.transitions.size == t._2("tran"))
      assert(aut.head.states.size == t._2("states"))

    }
  }

}
