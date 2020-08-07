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

import modelbuilding.core._
import modelbuilding.externalClients.opc.OPCSimulator

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
      ("GVL.R3", string),
      ("GVL.R4", string),
      ("GVL.S1", string),
      ("GVL.S2", string),
      ("GVL.S3", string),
      ("GVL.S4", string),
      ("GVL.S5", string),
      ("GVL.RESET", string)
    )
  )

  override val goalStates: Option[Set[StateMap]] = None

  val guardsC: Map[Command, Predicate] = Map(
    // The guards below were used for the case that there is no connection between machine 1 and machine 2
    load1   -> AND(EQ("GVL.S1", true), EQ("GVL.S2", false)), //make guard to be such that state is initial
    unload1 -> EQ("GVL.S2", true),
    load2   -> AND(EQ("GVL.S3", true), EQ("GVL.S4", false)),
    unload2 -> EQ("GVL.S4", true)
    // The guards below were used when there is a connection between machine 1 and machine 2
    /*load1 -> AND(EQ("GVL.S1", true), EQ("GVL.S2", false)), //make guard to be such that state is initial
    unload1 -> AND(EQ("GVL.S2", true), EQ("GVL.S3", false)),
    load2 -> AND(EQ("GVL.S3", true), EQ("GVL.S4", false)),
    unload2 -> AND(EQ("GVL.S4", true), EQ("GVL.S5", false)),*/
  )

  val actionsC: Map[Command, List[Action]] = Map(
    load1   -> List(Assign("GVL.R1", true)),
    unload1 -> List(Assign("GVL.R2", true)),
    load2   -> List(Assign("GVL.R3", true)),
    unload2 -> List(Assign("GVL.R4", true))
  )

  val postGuardsC: Map[Command, Predicate] = Map(
    // The guards below were used for the case that there is no connection between machine 1 and machine 2
    load1   -> EQ("GVL.S1", false), //make guard to be such that state is initial
    unload1 -> EQ("GVL.S2", false),
    load2   -> EQ("GVL.S3", false),
    unload2 -> EQ("GVL.S4", false)
    // The guards below were used when there is a connection between machine 1 and machine 2
    /*oad1 -> EQ("GVL.S2", true), //make guard to be such that state is initial
    unload1 -> EQ("GVL.S3", true),
    load2 -> EQ("GVL.S4", true),
    unload2 -> EQ("GVL.S5", true), */
  )

  //Remember: this can be if the action success or fails
  val postActionsC: Map[Command, List[Action]] = Map(
    load1   -> List(Assign("GVL.R1", false)),
    unload1 -> List(Assign("GVL.R2", false)),
    load2   -> List(Assign("GVL.R3", false)),
    unload2 -> List(Assign("GVL.R4", false))
  )

  override val guards: Map[String, Predicate] = guardsC.map(x => x._1.toString -> x._2)
  override val actions: Map[String, List[Action]] = actionsC.map(x => x._1.toString -> x._2)
  override val postGuards: Map[String, Predicate] = postGuardsC.map(x => x._1.toString -> x._2)
  override val postActions: Map[String, List[Action]] = postActionsC.map(x => x._1.toString -> x._2)
}
