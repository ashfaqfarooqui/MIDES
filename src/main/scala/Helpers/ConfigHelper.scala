package Helpers
import com.github.andr83.scalaconfig._
import com.typesafe.config.ConfigFactory

object ConfigHelper {
  val config: ScalaConfig = ConfigFactory.load()

  lazy val model  = config.asUnsafe[String]("main.Model")
  lazy val solver = config.asUnsafe[String]("main.Solver")

}
