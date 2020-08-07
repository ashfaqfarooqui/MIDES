package modelbuilding.models.WeldingRobots

import modelbuilding.core.interfaces.modeling.Specifications

class WeldingRobotsSpecification extends Specifications {
  import java.io.File
  override val specFilePath: Option[String] = Some(
    "SupremicaModels" + File.separator + "CatAndMouse.wmod"
  )
  addSpecsFromSupremica(specFilePath.get)
}

object WeldingRobotsSpecification {
  def apply(): WeldingRobotsSpecification = new WeldingRobotsSpecification()
}
