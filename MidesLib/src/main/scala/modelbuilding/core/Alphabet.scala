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

package modelbuilding.core

object Alphabet {

  //def apply(_a: AnyRef*): Alphabet = Alphabet(false,_a)

  def apply(_a: AnyRef*): Alphabet = {
    if (_a.isEmpty) new Alphabet(Set.empty[Symbol])
    else {
      val a = _a.head match {
        case _: Symbol  => _a.toSet.asInstanceOf[Set[Symbol]]
        case _: Command => _a.toSet.asInstanceOf[Set[Command]].map(Symbol)
        case t =>
          throw new IllegalArgumentException(
            s"Alphabet only accept inputs of either Seq[Symbol] or Seq[Command], not `${t.getClass}`"
          )
      }
      new Alphabet(a)
    }
  }

}

case class Alphabet(
    _a: Set[Symbol],
    includeTau: Boolean = false,
    includeReset: Boolean = false) {
  val events: Set[Symbol] = _a union (if (includeTau) Set(Symbol(tau))
                                      else Set.empty[Symbol]) union (if (includeReset)
                                                                       Set(Symbol(reset))
                                                                     else
                                                                       Set.empty[Symbol])

  def +(that: Alphabet): Alphabet = new Alphabet(this.events union that.events)
  override def toString: String   = s"Alphabet(${events.mkString(", ")})"
}
