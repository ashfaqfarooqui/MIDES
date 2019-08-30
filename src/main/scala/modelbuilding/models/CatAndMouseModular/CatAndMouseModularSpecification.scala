package modelbuilding.models.CatAndMouseModular

import modelbuilding.core.modeling.Specifications
import modelbuilding.models.CatAndMouseModular.CatAndMouseModular.addSpecsFromSupremica

class CatAndMouseModularSpecification extends Specifications{
  override val specFilePath: Option[String] = Some("SupremicaModels/CatAndMouse.wmod")
  addSpecsFromSupremica(specFilePath.get)
}
object CatAndMouseModularSpecification{
  def apply(): CatAndMouseModularSpecification = new CatAndMouseModularSpecification()
}