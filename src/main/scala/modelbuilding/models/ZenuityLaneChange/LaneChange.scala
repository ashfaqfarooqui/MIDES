package modelbuilding.models.ZenuityLaneChange

import modelbuilding.core.{Alphabet, StateSet}
import modelbuilding.core.modeling.MonolithicModel

object LaneChange extends MonolithicModel {
  override val name: String = "LaneChange"
  override val alphabet: Alphabet = Alphabet(
    goRight,
    goLeft,
    cancelRequest,
    b1true,
    b2true,
    b3true,
    b4true,
    b5true,
    b6true,
    b7true,
    b1false,
    b2false,
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
  override val states: StateSet = StateSet("laneChg")
}
