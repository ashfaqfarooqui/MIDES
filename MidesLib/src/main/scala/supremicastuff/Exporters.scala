package supremicastuff

import net.sourceforge.waters.subject.module.ModuleSubject
import java.io.File
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller
import java.{util => ju}

trait Exporters extends SupremicaBase {

  def saveToWMODFile(iFilePath: String, iModule: ModuleSubject = mModule): Boolean = {
    try {
      val file = new File(
        iFilePath + (if (!iFilePath.endsWith(".wmod")) iModule.getName + ".wmod"
                     else "")
      )
      val marshaller = new SAXModuleMarshaller(mFactory, mOptable)
      iModule.setComment(
        (if (getComment != null) getComment + "\n"
         else "") + "File generated: " + ju.Calendar
          .getInstance()
          .getTime
      )
      marshaller.marshal(iModule, file)
      return true
    } catch {
      case t: Throwable => println(t)
    }
    false
  }

}
