package modelbuilding.models.AGV

import modelbuilding.core.interfaces.modeling.Specifications

class AGVSpecifications extends Specifications {
  import java.io.File
  override val specFilePath: Option[String] = Some(
    "SupremicaModels" + File.separator + "AGV.wmod"
  )
  addSpecsFromSupremica(specFilePath.get)
}
object AGVSpecifications {
  def apply(): AGVSpecifications = new AGVSpecifications()
}
