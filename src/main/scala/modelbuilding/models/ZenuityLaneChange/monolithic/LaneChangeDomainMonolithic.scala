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

package modelbuilding.models.ZenuityLaneChange.monolithic

import modelbuilding.core.{Command, Controllable}

trait LaneChangeDomainMonolithic {
  override def toString: String = this match {
    case `right`  => "goRight"
    case `left`   => "goLeft"
    case `cancel` => "cancelRequest"
    case `b1`     => "b1"

  }
}
case object left   extends Command with LaneChangeDomainMonolithic with Controllable
case object right  extends Command with LaneChangeDomainMonolithic with Controllable
case object cancel extends Command with LaneChangeDomainMonolithic with Controllable
case object b1     extends Command with LaneChangeDomainMonolithic with Controllable
