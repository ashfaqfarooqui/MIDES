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

package modelbuilding.externalClients.matlab

import java.util.concurrent.TimeUnit

import com.mathworks.engine.MatlabEngine
import grizzled.slf4j.Logging
import modelbuilding.core.StateMap

class MatlabClient extends Logging {

  private var mEngine: Option[MatlabEngine] = None

  def MatlabClient = {

    getEngine
  }
  def getEngine = {
    if (mEngine.isDefined) mEngine.get
    else startMatlab.get
  }

  def startMatlab = {

    if (mEngine.isEmpty) {
      val matlabSession = MatlabEngine.findMatlab()
      println(s"fount matlab session: ${matlabSession}")
      mEngine = if (matlabSession.nonEmpty) {
        println("connecting to found session")
        Some(MatlabEngine.connectMatlab(matlabSession.head))

      } else Some(MatlabEngine.startMatlab())
    }
    println("connected")
    //if (mEngine.isEmpty) mEngine = Some(MatlabEngine.startMatlab())
    mEngine
  }

  def closeMatlab() = getEngine.disconnect()

  def runFunction(retArgs: Int, functionName: String, param: java.lang.Object) = {
    getEngine.feval[java.lang.Object](retArgs, functionName, param)
  }

  def reset = {
    getEngine.eval("reset")
  }

  def loadPath(filePath: String): Unit = {
    getEngine
      .evalAsync("addpath(\"/home/ashfaqf/Code/ZenuityMatlab/\")")
      .get(30, TimeUnit.SECONDS)
    //Thread.sleep(10000)

  }

  def getState: StateMap = {

    val a = getEngine.feval[Array[java.lang.Object]](5, "initialize")
    def matlabResponseToMap(a: Array[java.lang.Object]) = {
      Map(
        "state"             -> a(0),
        "direction"         -> a(1),
        "laneChangeRequest" -> a(2),
        "b1"                -> a(3),
        "b2"                -> a(4)
      )
    }
    val state = StateMap(matlabResponseToMap(a))
    println(state)
    state
  }

}

object MatlabClient {

  def apply(): MatlabClient = new MatlabClient()
  def destroy()             = MatlabClient().closeMatlab()
}
