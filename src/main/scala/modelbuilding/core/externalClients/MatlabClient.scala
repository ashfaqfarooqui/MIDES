package modelbuilding.core.externalClients

import java.util
import java.util.concurrent.TimeUnit

import com.mathworks.engine.MatlabEngine
import grizzled.slf4j.Logging
import modelbuilding.core.StateMap
import com.mathworks.matlab.types.Struct
import com.mathworks.mvm.MvmFactory

import scala.collection.JavaConverters._

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
        "state"           -> a(0),
        "direction"       -> a(1),
        "laneChngRequest" -> a(2),
        "b1"              -> a(3),
        "b2"              -> a(4)
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
