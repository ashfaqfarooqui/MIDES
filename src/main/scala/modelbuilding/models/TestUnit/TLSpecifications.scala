package modelbuilding.models.TestUnit

import modelbuilding.core.{Alphabet, SUL, StateSet}
import modelbuilding.core.modeling.Specifications
import modelbuilding.models.TestUnit.TransferLine.addSpecsFromSupremica

class TLSpecifications extends Specifications{
  override val specFilePath: Option[String] = Some("SupremicaModels/TransferLine.wmod")
  addSpecsFromSupremica(specFilePath.get)

}
object TLSpecifications {
  def apply(): TLSpecifications = new TLSpecifications()

}