package Helpers
import com.github.andr83.scalaconfig._
import com.typesafe.config.ConfigFactory

object ConfigHelper {
  val config: ScalaConfig = ConfigFactory.load()

  def getConfigAsString(k: String) = config.asUnsafe[String](k)
  def getConfigAsInt(k: String)    = config.asUnsafe[Int](k)
  def getConfigAsBool(k: String)   = config.asUnsafe[Boolean](k)

}
