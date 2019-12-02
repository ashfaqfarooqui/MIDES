package modelbuilding.models.ZenuityLaneChange

import modelbuilding.core.{Alphabet, StateSet}
import modelbuilding.core.modeling.MonolithicModel

object LaneChange extends MonolithicModel {

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
// //    b11false,
//     // b12false,
//     // b3false,
//     // b4false,
//     // b5false,
//     // b6false,
//     // b7false,
//     // b8false,
     b8true,
// //    b9false,
     b9true,
// //    b10false,
     b10true

  )

 

  val stateString: String       = "state direction laneChangeRequest b1 b2"
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

}
