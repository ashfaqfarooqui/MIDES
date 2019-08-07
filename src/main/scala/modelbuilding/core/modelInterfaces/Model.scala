package modelbuilding.core.modelInterfaces

import modelbuilding.core.modelInterfaces.ModularModel.Module
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

object ModularModel {
  type Module = String
}
trait ModularModel extends Model {
  override val isModular = true
  val modules: Set[Module]
  val states: StateSet
  def stateMapping: Map[Module,StateSet]
  def eventMapping: Map[Module,Alphabet]
  lazy val ns: Int = modules.size
}
