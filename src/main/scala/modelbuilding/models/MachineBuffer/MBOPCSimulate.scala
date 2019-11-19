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

package modelbuilding.models.MachineBuffer

import modelbuilding.core.simulators.OPCSimulator
import modelbuilding.core._

class MBOPCSimulate extends OPCSimulator {

  /**
    * Define state we are interested in. These state variables will be synced with the actual system.
    */
  val m1 = "m1"
  val m2 = "m2"

  val run       = "run"
  val execState = "state"
  val string    = "string"

  override val stateExecVariable: String      = "GVL.S1"
  override val stateExecFinishedValue: String = "true"

  override val variableList = Some(
    List(
      ("GVL.R1", string),
      ("GVL.R2", string),
      ("GVL.S1", string),
      ("GVL.S2", string),
      ("S3", string),
      ("GVL.RESET", string)
    )
  )

  override val goalStates: Option[Set[StateMap]] = None

  override val guards: Map[Command, Predicate] = Map(
    load1   -> AND(EQ("GVL.S1", true), EQ("GVL.S2", false)), //make guard to be such that state is initial
    unload1 -> AND(EQ("GVL.S2", true), EQ("GVL.S3", false)),
    load2   -> EQ("GVL.Load_R2_initial", true),
    unload2 -> EQ("GVL.Unload_R2_initial", true)
  )

  override val actions: Map[Command, List[Action]] = Map(
    load1   -> List(Assign("GVL.R1", true)),
    unload1 -> List(Assign("GVL.R2", true)),
    load2   -> List(Assign("GVL.R3", true)),
    unload2 -> List(Assign("GVL.R4", true))
  )

  override val postGuards: Map[Command, Predicate] = Map(
    load1   -> EQ("GVL.S2", true), //make guard to be such that state is initial
    unload1 -> EQ("GVL.S3", true),
    load2   -> EQ("GVL.Load_R2_finish", true),
    unload2 -> EQ("GVL.Unload_R2_finish", true)
  )

  //Remember: this can be if the action succeds or fails
  override val postActions: Map[Command, List[Action]] = Map(
    load1   -> List(Assign("GVL.R1", false)),
    unload1 -> List(Assign("GVL.R2", false)),
    load2   -> List(Assign("GVL.R3", false)),
    unload2 -> List(Assign("GVL.R4", false))
  )

}
