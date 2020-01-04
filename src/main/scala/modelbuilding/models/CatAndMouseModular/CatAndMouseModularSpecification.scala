package modelbuilding.models.CatAndMouseModular

import modelbuilding.core.interfaces.modeling.Specifications

class CatAndMouseModularSpecification extends Specifications {
  import java.io.File
  override val specFilePath: Option[String] = Some(
    "SupremicaModels" + File.separator + "CatAndMouse.wmod"
  )
  addSpecsFromSupremica(specFilePath.get)
}
object CatAndMouseModularSpecification {
  def apply(): CatAndMouseModularSpecification = new CatAndMouseModularSpecification()
}
