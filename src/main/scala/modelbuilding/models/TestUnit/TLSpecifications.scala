package modelbuilding.models.TestUnit

import modelbuilding.core.modeling.Specifications

class TLSpecifications extends Specifications {
  import java.io.File
  override val specFilePath: Option[String] = Some(
    "SupremicaModels" + File.separator + "TransferLine.wmod"
  )
  addSpecsFromSupremica(specFilePath.get)

}
object TLSpecifications {
  def apply(): TLSpecifications = new TLSpecifications()

}
