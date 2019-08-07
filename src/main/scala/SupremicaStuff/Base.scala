package SupremicaStuff

import java.io.File
import java.util.Calendar

import net.sourceforge.waters.model.compiler.{CompilerOperatorTable, ModuleCompiler}
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.model.marshaller.{DocumentManager, JAXBModuleMarshaller}
import net.sourceforge.waters.model.module.EventDeclProxy
import net.sourceforge.waters.plain.des.ProductDESElementFactory
import net.sourceforge.waters.subject.module.{ModuleSubject, ModuleSubjectFactory}
import net.sourceforge.waters.xsd.base.EventKind
import org.supremica.automata
import org.supremica.automata.IO.{AutomataToXML, ProjectBuildFromXML}
import org.supremica.automata.Project
import org.supremica.properties.Config

import scala.collection.JavaConverters._

trait Base extends SimpleModuleFactory {
  val mModule: ModuleSubject //= ReadFromWmodFileModuleFactory("supremicaFiles/controlabilitytest.xml").get
  lazy val mFactory = new ModuleSubjectFactory()
  lazy val mOptable = CompilerOperatorTable.getInstance()
  lazy val mParser = new ExpressionParser(mFactory, mOptable)
  lazy val mDocumentManager = new DocumentManager()

  //lazy val sProject = ReadFromWmodFileModuleFactory("supremicaFiles/controlabilitytest.xml")
  def getComment = if (mModule.getComment() == null) "" else s"${mModule.getComment()}\n"


  def saveToXMLFile(iFilePath: String = "./supremicaFiles/file.xml", aut: automata.Automata): Boolean = {
    try {
      val file = new File(iFilePath)
      new AutomataToXML(aut).serialize(file)

      return true
    } catch {
      case t: Throwable => println(t)
    }
    false
  }


  def saveToWMODFile(iFilePath: String, iModule: ModuleSubject = mModule): Boolean = {
    try {
      val file = new File(iFilePath + (if (!iFilePath.endsWith(".wmod")) iModule.getName() + ".wmod" else ""))
      val marshaller = new JAXBModuleMarshaller(mFactory, mOptable)
      iModule.setComment((if (getComment != null) getComment + "\n" else "") + "File generated: " + Calendar.getInstance().getTime())
      marshaller.marshal(iModule, file)
      return true
    } catch {
      case t: Throwable => println(t)
    }
    false
  }


  object ReadProjectFromXML {
    def apply(path: String): Option[Project] = {
      try {
        val fileUrl = new File(path).toURL
        return Some(new ProjectBuildFromXML().build(fileUrl))
      }
      catch {
        case t: Throwable => println(t)
      }
      None
    }
  }


  object ReadFromWmodFile {
    def apply(iFilePath: String): Option[ModuleSubject] = {
      try {
        val fileUri = (new File(iFilePath)).toURI()
        val marshaller = new JAXBModuleMarshaller(new ModuleSubjectFactory(), CompilerOperatorTable.getInstance())
        return Some(marshaller.unmarshal(fileUri).asInstanceOf[ModuleSubject])
      } catch {
        case t: Throwable => println(t)
      }
      None
    }
  }

  def getWatersModule(module: ModuleSubject = mModule, hasProperties: Boolean = false) = {
    if (module == null) throw new NullPointerException("argument must be non null")
    else {
      val expand = Config.EXPAND_EXTENDED_AUTOMATA.isTrue()
      val factory = ProductDESElementFactory.getInstance()
      val optimize = Config.OPTIMIZING_COMPILER.isTrue()
      val compiler = new ModuleCompiler(mDocumentManager, factory, module)
      compiler.setOptimizationEnabled(optimize)
      compiler.setExpandingEFATransitions(expand)
      if (!hasProperties) {
        compiler.setEnabledPropositionNames(List.empty.asJavaCollection)
      }

      compiler.compile()
    }
  }
}

//To create a new Module Subject....
trait SimpleModuleFactory {
  def moduleFactory(iModuleName: String, iModuleComment: Option[String] = None) = {
    val ms = new ModuleSubject(iModuleName, null)
    iModuleComment match {
      case Some(comment) => ms.setComment(comment)
      case _ =>
    }
    ms
  }

  def initModule(ms: ModuleSubject) = { //Add marking to module
    ms.getEventDeclListModifiable().add(new ModuleSubjectFactory().createEventDeclProxy(new ModuleSubjectFactory().createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME), EventKind.PROPOSITION));
    ms
  }

}
object SimpleModuleFactory extends SimpleModuleFactory {
  def apply(iModuleName: String, iModuleComment: Option[String] = None): ModuleSubject = initModule(moduleFactory(iModuleName, iModuleComment))
}
