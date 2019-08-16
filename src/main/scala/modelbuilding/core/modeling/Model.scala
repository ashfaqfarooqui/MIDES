package modelbuilding.core.modeling

import modelbuilding.core.simulation.SUL
import modelbuilding.core.{Alphabet, StateSet}

trait Model{

  val name: String
  val alphabet: Alphabet
  val simulation: SUL
  val states: StateSet

  val specFilePath: Option[String] = None // Depricated, used before the introduction of modelbuilding.core.modeling.Specifications

  val isModular: Boolean = false
  val hasSpecs: Boolean = false

//  def eval(t: StateMapTransition): Boolean = false // overriden by the extension of modelbuilding.core.modeling.Specifications

}


trait MonolithicModel extends Model

trait ModularModel extends Model {
  override val isModular = true
  val modules: Set[String]
  def stateMapping: Map[String,StateSet]
  def eventMapping: Map[String,Alphabet]
  lazy val ns: Int = modules.size
}
case class Module(name: String, stateSet: StateSet, alphabet: Alphabet, specs: Set[String] = Set.empty[String])