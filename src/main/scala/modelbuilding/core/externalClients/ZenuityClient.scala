package modelbuilding.core.externalClients

import com.mathworks.engine.MatlabEngine

class ZenuityClient {


  private var mEngine :Option[MatlabEngine]= None

  def getEngine = {
    if(mEngine.isDefined) mEngine.get
    else startMatlab.get
  }

  def startMatlab = {
    if(mEngine.isEmpty) mEngine=Some(MatlabEngine.startMatlab())
    mEngine
  }

  def closeMatlab = MatlabEngine.connectMatlab()


}

object ZenuityClient {

  def apply(): ZenuityClient = new ZenuityClient()
  def closeMatlab(c:ZenuityClient) = c.getEngine.disconnect()
}
