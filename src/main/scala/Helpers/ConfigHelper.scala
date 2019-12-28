/*
 * Learning Automata for Supervisory Synthesis
 *  Copyright (C) 2019
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Helpers
import com.github.andr83.scalaconfig._
import com.typesafe.config.ConfigFactory

object ConfigHelper {
  val config: ScalaConfig = ConfigFactory.load()

  lazy val model: String           = config.asUnsafe[String]("main.Model")
  lazy val solver: String          = config.asUnsafe[String]("main.Solver")
  lazy val outputDirectory: String = config.asUnsafe[String]("main.OutputDirectory")
  lazy val url: String             = config.asUnsafe[String]("opc.url")
  lazy val runner_timeout: Int     = config.asUnsafe[Int]("opc.runner_timeout")
  lazy val matlabPath: String      = config.asUnsafe[String]("matlab.program.path")
  lazy val matlabProgram: String   = config.asUnsafe[String]("matlab.program.name")

}
