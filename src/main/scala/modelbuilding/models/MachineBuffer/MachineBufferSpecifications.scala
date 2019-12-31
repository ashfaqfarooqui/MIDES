package modelbuilding.models.MachineBuffer

import modelbuilding.core.interfaces.modeling.Specifications

class MachineBufferSpecifications extends Specifications {
  // Add all specifications available to the model
  import java.io.File
  override val specFilePath: Option[String] = Some(
    "SupremicaModels" + File.separator + "MachineBufferMachine.wmod"
  )
  addSpecsFromSupremica(specFilePath.get)
}
object MachineBufferSpecifications {
  def apply(): MachineBufferSpecifications = new MachineBufferSpecifications()
}
