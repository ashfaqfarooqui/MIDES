package modelbuilding.externalClients.TraCI

import org.eclipse.sumo.libsumo.{Simulation, StringVector}
import org.eclipse.sumo.libsumo._

class SUMOClient {

  def startSimulation =   Simulation.start(new StringVector(Array[String]("sumo", "-c", "test.sumocfg")))

  def stepSimulation = Simulation.step()

  def editParam()= ???

  def readData() = 

}
object SUMOClient{
  def apply() : SUMOClient = new SUMOClient()
}