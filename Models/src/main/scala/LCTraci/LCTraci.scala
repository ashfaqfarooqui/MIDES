package LCTraci

import modelbuilding.core.{Alphabet, StateSet}
import modelbuilding.core.interfaces.modeling.MonolithicModel

object LCTraci extends MonolithicModel{
  override val name: String = "LCTraci"
  override val alphabet: Alphabet = Alphabet(normalStep,faultyStep)
  override val states: StateSet = StateSet("time", "LCAs", "NrFault", "error","crash")
}
