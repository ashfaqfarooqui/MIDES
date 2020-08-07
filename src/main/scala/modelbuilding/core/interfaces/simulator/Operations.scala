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

package modelbuilding.core.interfaces.simulator

import modelbuilding.core.{Action, Command, Predicate}

/**
  * The following traits allow us to define operations that are two or three state.
  * Three state operations are an extension to two state operations but with additional guards and actions that are used to "turn off" the operation.
  * For example, in the [[Machinebuffer]] case when connecting to the simulation post guards are a way to check if the operation has completed.
  *
  */

trait ThreeStateOperation extends TwoStateOperation {
  val postGuards: Map[String, Predicate]
  val postActions: Map[String, List[Action]]
  def postGuards(c: Command): Predicate = postGuards(c.toString)
  def postActions(c: Command): List[Action] = postActions(c.toString)

}

trait TwoStateOperation {
  val guards: Map[String, Predicate]
  val actions: Map[String, List[Action]]
  def guards(c: Command): Predicate = guards(c.toString)
  def actions(c: Command): List[Action] = actions(c.toString)

}
