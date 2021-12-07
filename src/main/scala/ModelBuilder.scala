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

import grizzled.slf4j.Logging
import modelbuilding.core.{SUL, Transition}
import modelbuilding.helpers.ConfigHelper
import modelbuilding.models.TestUnit.TLSpecifications
import modelbuilding.models.ZenuityLaneChange.LaneChangeMoreInputs.LaneChangeSimulateMonolithic
import modelbuilding.models.ZenuityLaneChange.LaneChangeModular.LaneChangeSimulateModular
import modelbuilding.models._
import modelbuilding.solvers._
import supremicastuff.SupremicaHelpers

object ModelBuilder extends Logging {

  val modelName: String = ConfigHelper.model  //"MachineBufferNoSpec"
  val solver: String    = ConfigHelper.solver //"LStarPlantLearner" // "modular", "mono"

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
    case "LaneChangeModular" =>
      SUL(
        ZenuityLaneChange.LaneChangeModular.LaneChange,
        new LaneChangeSimulateModular,
        None,
        false
      )
    case "WeldingRobots" =>
      val robots = 1
      val tasks = 11
      SUL(
        new WeldingRobots.WeldingRobots(robots, tasks),
        new WeldingRobots.WeldingRobotsSimulation(robots, tasks),
        None,
        false
      )
    case _ => throw new Exception("A model wasn't defined.")
  }

  def main(args: Array[String]): Unit = {

    //info(s"Running sul: $sul")
    //    info(s"Starting learner for : $modelName, using $solver as solver")

    //    val result = solver match {
    //      case "ModularPlantLearnerWithPartialStates" =>
    //        new FrehagePlantBuilderWithPartialStates(sul)
    //      case "ModularPlantLearner"      => new FrehagePlantBuilder(sul)
    //      case "ModularPlantLearnerWithoutTau"      => new FrehagePlantBuilderWithoutTau(sul)
    //      case "ModularPlantLearnerWithoutTauNew"      => new FrehagePlantBuilderWithoutTauNew(sul)
    //      case "ModularSupervisorLearner" => new FrehageModularSupSynthesis(sul)
    //      case "MonolithicPlantSolver"    => new MonolithicSolver(sul)
    //      case "MonolithicSupSolver"      => new MonolithicSupSolver(sul)
    //      case "ModularSupSolver"         => new ModularSupSolver(sul)
    //      case "LStarPlantLearner"        => new LStarPlantSolver(sul)
    //      case "LStarSupervisorLearner"   => new LStarSuprSolver(sul)
    //      case "CompositionalOptimization"   => new FrehageCompositionalOptimization(sul)
    //
    //    }

    //    info("Learning done!, writing results")
    //
    //    println("Queries to the simulator: ", sul.simQueries)
    //
    //    val automata = result.getAutomata

    //    automata.modules foreach println
    //    automata.modules.foreach(_.createDotFile)
    //    SupremicaHelpers.exportAsSupremicaAutomata(automata, name = modelName)


    /*
     * Experiments for Hagebring_TASE2021
     */

    def solve(r:Int,t:Int,s:Int): (Long,Long,Long) = {
      val t0 = System.nanoTime()
      val sul = SUL(
        model = new WeldingRobots.WeldingRobots(r, t),
        simulator = new WeldingRobots.WeldingRobotsSimulation(r, t, seed = s),
        specification = None,
        acceptsPartialStates = false
      )
//      val sol = new FrehageCompositionalOptimization(sul)
      val sol = new FrehageCompositionalOptimizationNEW(sul)
      (sol.maxSize,sul.simQueries,System.nanoTime()-t0)
    }
    // Prevent bias to setup time during experiments
    solve(3,3,1);solve(3,3,1);solve(3,3,1)


    /*
     * 3:   3-10
     * 4:   3-10
     * 5:   3-8
     * 6:
     * 7:
     * 8:
     * 9:
     * 10:
     */
    val (r_min, r_max) = (10, 10)
    val (t_min, t_max) = (10, 10)

    val seeds = 1 to 1

    val t0 = System.nanoTime()
    var t1: Long = 0
    var t2: Long = 0
    var t3: Long = 0
    var t4: Long = 0
    var t5: Long = 0
    var t6: Long = 0
    var t7: Long = 0
    for (s <- seeds; r <- r_min to r_max; t <- t_min to t_max) {

      val (maxSize,simQueries,time) = solve(r,t,s)
      println(s"$r $t $s $maxSize $simQueries $time")

//      val (sol1, sol2) = solve(r,t,s)
//      t1 += sol1.time_total
//      t2 += sol2.time_total
//      t3 += sol2.t1
//      t4+= sol2.t2
//      t5 += sol2.t3
//      t6 += sol1.moduleTransitions.map(_._2.size).sum
//      t7 += sol2.moduleTransitions.map(_._2.size).sum

    }
//    println(s"TIME: ${t1/1000000} ${t2/1000000} ${t3/1000000} ${t4/1000000} ${t5/1000000} $t6 $t7")

  }



}
