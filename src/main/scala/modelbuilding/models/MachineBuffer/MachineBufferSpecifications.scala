package modelbuilding.models.MachineBuffer

import modelbuilding.core.modeling.Specifications

class MachineBufferSpecifications extends Specifications {
  // Add all specifications available to the model
  override val specFilePath: Option[String] = Some(
    "SupremicaModels\\MachineBufferMachine.wmod"
  )
  addSpecsFromSupremica(specFilePath.get)
}
object MachineBufferSpecifications {
  def apply(): MachineBufferSpecifications = new MachineBufferSpecifications()
}
