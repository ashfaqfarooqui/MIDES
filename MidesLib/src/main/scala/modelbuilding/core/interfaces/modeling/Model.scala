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

package modelbuilding.core.interfaces.modeling

import modelbuilding.core.{Alphabet, StateSet}

trait Model {

  val name: String
  val alphabet: Alphabet
  val states: StateSet

  val specFilePath: Option[String] =
    None // Depricated, used before the introduction of modelbuilding.core.interfaces.modelInterfaces.Specifications

  val isModular: Boolean = false
  val hasSpecs: Boolean  = false

  //  def eval(t: StateMapTransition): Boolean = false // overriden by the extension of modelbuilding.core.interfaces.modelInterfaces.Specifications

}

trait MonolithicModel extends Model

trait ModularModel extends Model {
  override val isModular = true
  val modules: Set[String]

  def stateMapping: Map[String, StateSet]

  def eventMapping: Map[String, Alphabet]

  lazy val ns: Int = modules.size
}

case class Module(
    name: String,
    stateSet: StateSet,
    alphabet: Alphabet,
    specs: Set[String] = Set.empty[String])
