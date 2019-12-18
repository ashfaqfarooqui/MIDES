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

import modelbuilding.core.modeling.MonolithicModel
import modelbuilding.core.{
  Symbol,
  Action,
  Alphabet,
  AlwaysTrue,
  Assign,
  Command,
  EventC,
  Predicate,
  StateSet,
  EQ,
  AND,
  OR
}

object LaneChangeMonolithic extends MonolithicModel {

  val ops: (Set[EventC], Map[Command, Predicate], Map[Command, List[Action]]) = {
    val ips                                     = (4 to 12).map(x => s"b$x").toSet
    val subsets                                 = ips.subsets
    var tempGuards: Map[Command, Predicate]     = Map.empty[Command, Predicate]
    var tempActions: Map[Command, List[Action]] = Map.empty[Command, List[Action]]
    val events: Set[EventC] = subsets.map { s =>
      if(s.isEmpty){
        EventC("empty")
      }else EventC(s.mkString)
    }.toSet

    val preds:Map[String,Predicate] = Map(
      "b4"->OR(List(EQ("state","stateB"),EQ("state","stateC"),EQ("state","stateD"))),
      "b5"->OR(List(EQ("state","stateB"),EQ("state","stateD"))),
      "b6"->EQ("state","stateB"),
      "b7"->EQ("state","stateC"),
      "b8"->EQ("state","stateD"),
      "b9"->EQ("state","stateD"),
      "b10"->EQ("state","stateE"),
      "b11"->EQ("state","stateE"),
      "b12"->EQ("state","stateE"),
"empty" -> OR(List(EQ("state","stateF"),EQ("state","stateG")))

    )
    events.foreach { e =>
      tempGuards = tempGuards + (e -> AND(ips.filter(i=>e.name.contains(i)||e.name=="empty").map(preds).toList))
      tempActions = tempActions + (e -> ips
        .filter(i => e.toString.contains(i))
        .map(x => Assign(x, true))
        .toList)
    }
    val additionalAlphabets: Set[EventC] = Set("left", "right", "none").map(EventC)
    additionalAlphabets.foreach { e =>
      tempGuards = tempGuards + (e   -> AlwaysTrue)
      tempActions = tempActions + (e -> List(Assign("laneChngReq", e.toString)))

    }
    (events union additionalAlphabets, tempGuards, tempActions)
  }
  override val name = "LaneChange"

  //subsets.map(_.mkString).foreach(println)

  //events foreach println
  val additionalAlphabets: Set[EventC] = Set("left", "right", "none").map(EventC)

  override val alphabet = new Alphabet(ops._1.map(Symbol))
  val stateString: String =
    "state direction laneChangeRequest b1 b2 laneChngReq"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

}
