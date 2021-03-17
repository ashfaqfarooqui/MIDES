package LaneChange

import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core.{Alphabet, StateSet}

object LaneChange extends ModularModel {

  override val name: String = "LaneChange"
  override val alphabet: Alphabet = Alphabet(
    goRight,
    goLeft,
    cancelRequest,
    b11true,
    b12true,
    b3true,
    b4true,
    b5true,
    b6true,
    b7true,
    b11false,
    b12false,
    b3false,
    b4false,
    b5false,
    b6false,
    b7false,
    b8false,
    b8true,
    b9false,
    b9true,
    b10false,
    b10true
  )

  val modules: Set[String] = "M2,M3".split(",").toSet
  def stateMapping: Map[String, StateSet] = Map(
    // "M1" -> StateSet("state","direction","laneChangeRequest","b1","b2"),
    "M2" -> StateSet(
      "state",
      "direction",
      "laneChangeRequest",
      "b1",
      "b2",
      "laneChngReq",
      "b4",
      "b5",
      "b8",
      "b9"
    ),
    "M3" -> StateSet(
      "state",
      "direction",
      "laneChangeRequest",
      "b1",
      "b2",
      "laneChngReq",
      "b3",
      "b4",
      "b5",
      "b6"
    )
  )
  def eventMapping: Map[String, Alphabet] = Map(
    // "M1" -> Alphabet(
    //   goRight,
    //   goLeft,
    //   cancelRequest,
    //   b3true,
    //   b4true,
    //   b5true,
    //   b6true,
    //   b7true,//   b8true,
    //   b9true,
    //   b10true,
    //   b11true,
    //   b12true,
    //   b3false,
    //   b4false,
    //   b5false,
    //   b6false,
    //   b7false,
    //   b8false,
    //   b9false,
    //   b10false,
    //   b11false,
    //   b12false
    // ),
    "M2" -> Alphabet(
      goRight,
      goLeft,
      cancelRequest,
      b4true,
      b5true,
      b8true,
      b9true,
      b4false,
      b5false,
      b8false,
      b9false
    ),
    "M3" -> Alphabet(
      goRight,
      goLeft,
      cancelRequest,
      b3true,
      b3false,
      b4true,
      b4false,
      b5true,
      b5false,
      b6true,
      b6false
    )
  )

  val stateString: String =
    "state direction laneChangeRequest b1 b2 laneChngReq b3 b4 b5 b6 b7 b8 b9 b10 b11 b12"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

}
