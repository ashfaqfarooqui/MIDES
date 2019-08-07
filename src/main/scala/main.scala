
import main.scala.modelbuilding.solvers.FrehageSolver
import modelbuilding.core.modelInterfaces._
import modelbuilding.models.MachineBuffer.MachineBuffer
import modelbuilding.solvers._
import grizzled.slf4j.Logging
import scala.collection.JavaConverters._

object ModelBuilder extends Logging {

  val model: Model = MachineBuffer

  val solver: String = "frehage" // "modular", "mono"

  def main(args: Array[String]) : Unit= {

    info("Automata learn!")

    val result = solver match {
      case "frehage" => new FrehageSolver(model)
    }

    info("Automata display!")
    result.getAutomata.getAutomata.asScala foreach println

  }

}
