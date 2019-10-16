package modelbuilding.core.externalClients

import com.mathworks.engine.MatlabEngine
import grizzled.slf4j.Logging
import modelbuilding.core.StateMap
import com.mathworks.matlab.types.Struct
import scala.collection.JavaConverters._

class ZenuityClient extends Logging {

  private var mEngine: Option[MatlabEngine] = None

  def getEngine = {
    if (mEngine.isDefined) mEngine.get
    else startMatlab.get
  }

  def startMatlab = {

    if (mEngine.isEmpty) {
      val matlabSession = MatlabEngine.findMatlab()
      println(s"fount matlab session: $matlabSession")
      mEngine =
        if (matlabSession.nonEmpty) Some(MatlabEngine.connectMatlab(matlabSession.head))
        else Some(MatlabEngine.startMatlab())
    }
    //if (mEngine.isEmpty) mEngine = Some(MatlabEngine.startMatlab())
    mEngine
  }

  def closeMatlab = getEngine.disconnect()

  def runProgram(param: AnyRef*) = {
    getEngine.feval("update", param)
  }

  def reset = {
    getEngine.eval("reset")
  }

  def getState: StateMap = {
    val s: Struct = getEngine.getVariable("self")
    //StateMap(getEngine.eval("getState"))
    StateMap(s.keySet().asScala.map(x => x -> s.get(x)).toMap)
  }

  def setState(param: AnyRef*) = getEngine.feval("setState", param)
}

object ZenuityClient {

  def apply(): ZenuityClient        = new ZenuityClient()
  def closeMatlab(c: ZenuityClient) = c.getEngine.disconnect()
}
