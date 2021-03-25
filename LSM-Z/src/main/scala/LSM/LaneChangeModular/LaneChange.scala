package LSM.LaneChangeModular

import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core._

object LaneChange extends ModularModel {

  def createOperations(
      directionSet: Set[String] = Set("left", "right", "none"),
      ips: Set[String]
    ): (Set[EventC], Map[Command, Predicate], Map[Command, List[Action]]) = {
    //  val directionSet                            = Set("left", "right", "none")
    val additionalAlphabets: Set[EventC] = directionSet.map(EventC)
    //  val ips                                     = (4 to 12).map(x => s"b$x").toSet
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
    println(s"tempguards $tempGuards")
    println(s"tempactions $tempActions ")
    (events union additionalAlphabets, tempGuards, tempActions)
  }

  val evtsDuringStateA = createOperations(ips = Set.empty[String])
  val evtsDuringStateB = createOperations(ips = Set("b4", "b5", "b6"))
  val evtsDuringStateC = createOperations(ips = Set("b4", "b7"))
  val evtsDuringStateD = createOperations(ips = Set("b4", "b5", "b8", "b9"))
  val evtsDuringStateE = createOperations(ips = Set("b10", "b11", "b12"))

  val ops = (
    evtsDuringStateA._1 union evtsDuringStateB._1 union evtsDuringStateC._1 union evtsDuringStateD._1 union evtsDuringStateE._1,
    evtsDuringStateA._2 ++ evtsDuringStateB._2 ++ evtsDuringStateC._2 ++ evtsDuringStateD._2 ++ evtsDuringStateE._2,
    evtsDuringStateA._3 ++ evtsDuringStateB._3 ++ evtsDuringStateC._3 ++ evtsDuringStateD._3 ++ evtsDuringStateE._3
  )

  val stateString: String =
    "state direction laneChangeRequest b1 b2"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override val alphabet: Alphabet = new Alphabet(ops._1.map(Symbol))
  override val modules: Set[String] =
    Set("duringStateA", "duringStateB", "duringStateC", "duringStateD", "duringStateE")

  override def stateMapping: Map[String, StateSet] = modules.map(_ -> states).toMap
  override def eventMapping: Map[String, Alphabet] =
    Map(
      "duringStateA" -> new Alphabet(evtsDuringStateA._1.map(Symbol)),
      "duringStateB" -> new Alphabet(evtsDuringStateB._1.map(Symbol)),
      "duringStateC" -> new Alphabet(evtsDuringStateC._1.map(Symbol)),
      "duringStateD" -> new Alphabet(evtsDuringStateD._1.map(Symbol)),
      "duringStateE" -> new Alphabet(evtsDuringStateE._1.map(Symbol))
    )

  override val name = "LaneChangeModular"

  //subsets.map(_.mkString).foreach(println)

  //events foreach println
  //val additionalAlphabets: Set[EventC] = Set("left", "right", "none").map(EventC)

  //override val alphabet = new Alphabet(ops._1.map(Symbol))
}
