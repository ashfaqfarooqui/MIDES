package modelbuilding.core.modelInterfaces

import modelbuilding.core.{Alphabet, StateSet}

trait Model{
  val name: String
  val alphabet: Alphabet
  val simulation: SUL

  val isModular: Boolean
}

trait MonolithicModel extends Model {
  override val isModular = false
}

trait ModularModel extends Model {
  override val isModular = true
  val modules: Set[String]
  val states: StateSet
  def stateMapping: Map[String,StateSet]
  def eventMapping: Map[String,Alphabet]
}
