package LaneChange.LaneChangeMoreInputs

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

import modelbuilding.core.interfaces.modeling.MonolithicModel
import modelbuilding.core.{
  AND,
  Action,
  Alphabet,
  AlwaysTrue,
  Assign,
  Command,
  EQ,
  EventC,
  OR,
  Predicate,
  StateSet,
  Symbol
}

object LaneChange extends MonolithicModel {

  val ops: (Set[EventC], Map[Command, Predicate], Map[Command, List[Action]]) = {
    val directionSet                            = Set("left", "right", "none")
    val additionalAlphabets: Set[EventC]        = directionSet.map(EventC)
    val ips                                     = (4 to 12).map(x => s"b$x").toSet
    val subsets                                 = ips.subsets.flatMap(x => directionSet.map(y => (x union Set(y))))
    var tempGuards: Map[Command, Predicate]     = Map.empty[Command, Predicate]
    var tempActions: Map[Command, List[Action]] = Map.empty[Command, List[Action]]
    val events: Set[EventC] = subsets.map { s =>
      if (s.isEmpty) {
        EventC("empty")
      } else EventC(s.mkString)
    }.toSet

    val preds: Map[String, Predicate] = Map(
      "b4" -> OR(
        List(EQ("state", "stateB"), EQ("state", "stateC"), EQ("state", "stateD"))
      ),
      "b5"    -> OR(List(EQ("state", "stateB"), EQ("state", "stateD"))),
      "b6"    -> EQ("state", "stateB"),
      "b7"    -> EQ("state", "stateC"),
      "b8"    -> EQ("state", "stateD"),
      "b9"    -> EQ("state", "stateD"),
      "b10"   -> EQ("state", "stateE"),
      "b11"   -> EQ("state", "stateE"),
      "b12"   -> EQ("state", "stateE"),
      "empty" -> OR(List(EQ("state", "stateF"), EQ("state", "stateG"))),
      "left"  -> AlwaysTrue,
      "right" -> AlwaysTrue,
      "none"  -> AlwaysTrue
    )
    events.foreach { e =>
      tempGuards = tempGuards + (e -> AND(
        (ips union directionSet)
          .filter(i => e.name.contains(i) || e.name == "empty")
          .map(preds)
          .toList
      ))
      tempActions = tempActions + (e -> (ips union directionSet)
        .filter(i => e.toString.contains(i))
        .map(x =>
          if (ips.contains(x)) Assign(x, true)
          else Assign("laneChngReq", x.toString)
        )
        .toList)
    }
    additionalAlphabets.foreach { e =>
      tempGuards = tempGuards + (e -> AlwaysTrue)
      tempActions = tempActions + (e -> tempActions(e).++(
        List(Assign("laneChngReq", e.toString))
      ))

    }
//    println(s"tempguards $tempGuards")
//    println(s"tempactions $tempActions ")
    (events union additionalAlphabets, tempGuards, tempActions)
  }
  override val name = "LaneChange"

  //subsets.map(_.mkString).foreach(println)

  //events foreach println
  //val additionalAlphabets: Set[EventC] = Set("left", "right", "none").map(EventC)

  override val alphabet = new Alphabet(ops._1.map(Symbol))
  val stateString: String =
    "state direction laneChangeRequest b1 b2"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

}
