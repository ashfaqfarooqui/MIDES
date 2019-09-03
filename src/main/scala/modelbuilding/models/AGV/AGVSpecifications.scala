package modelbuilding.models.AGV

import modelbuilding.core.modeling.Specifications

class AGVSpecifications extends Specifications {
  override val specFilePath: Option[String] = Some("SupremicaModels/AGV.wmod")
  addSpecsFromSupremica(specFilePath.get)
}
object AGVSpecifications{
  def apply(): AGVSpecifications = new AGVSpecifications()
}
