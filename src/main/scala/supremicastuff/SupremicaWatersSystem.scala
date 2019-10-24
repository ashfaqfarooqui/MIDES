package supremicastuff

import java.io.File
import java.util.Calendar

import grizzled.slf4j.Logging
import net.sourceforge.waters.model.base.EventKind
import net.sourceforge.waters.model.compiler.{
  CompilerOperatorTable,
  ModuleCompiler
}
import net.sourceforge.waters.model.des.ProductDESProxy
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.model.marshaller.{
  DocumentManager,
  SAXModuleMarshaller
}
import net.sourceforge.waters.model.module.EventDeclProxy
import net.sourceforge.waters.plain.des.ProductDESElementFactory
import net.sourceforge.waters.subject.module.{
  ModuleSubject,
  ModuleSubjectFactory
}
import org.supremica.automata
import org.supremica.automata.Automata
import org.supremica.automata.IO.ProjectBuildFromWaters
import org.supremica.automata.algorithms._
import org.supremica.automata.algorithms.minimization.MinimizationOptions
import org.supremica.properties.Config

import scala.collection.JavaConverters._

object SupremicaWatersSystem {
  def apply(iFilePath: String): SupremicaWatersSystem = {
    try {
      val fileUri = new File(iFilePath).toURI
      val marshaller = new SAXModuleMarshaller(
        new ModuleSubjectFactory(),
        CompilerOperatorTable.getInstance()
      )
      new SupremicaWatersSystem(
        marshaller.unmarshal(fileUri).asInstanceOf[ModuleSubject]
      )
    } catch {
      case t: Throwable =>
        throw new IllegalArgumentException(
          s"Could not generate SupremicaWatersSystem from file `$iFilePath`, error: $t"
        )
    }
  }
}
class SupremicaWatersSystem(
  val mModule: ModuleSubject /* e.g. ReadSystemFromWmodFile("supremicaFiles/controlabilitytest.xml") */
) extends SimpleModuleFactory
    with Logging {

  assert(mModule != null, "The ModuleSubject must be non null")

  lazy val mFactory = new ModuleSubjectFactory()
  lazy val mOptable: CompilerOperatorTable = CompilerOperatorTable.getInstance()
  lazy val mParser = new ExpressionParser(mFactory, mOptable)
  lazy val mDocumentManager = new DocumentManager()

  lazy val getSupremicaAutomata: Automata = {
    try {
      val proj = new ProjectBuildFromWaters(new DocumentManager())
      //Config.OPTIMIZING_COMPILER.set(true)
      proj.build(mModule).asInstanceOf[Automata]
    } catch {
      case t: Throwable => println(t); new Automata()
    }
  }
  lazy val getSupremicaPlants: Automata = getSupremicaAutomata.getPlantAutomata
  lazy val getSupremicaSpecs: Automata =
    getSupremicaAutomata.getSpecificationAutomata

  def getComment: String =
    if (mModule.getComment == null) "" else s"${mModule.getComment}\n"

  def saveToWMODFile(iFilePath: String,
                     iModule: ModuleSubject = mModule): Boolean = {
    try {
      val file = new File(
        iFilePath + (if (!iFilePath.endsWith(".wmod")) iModule.getName + ".wmod"
                     else "")
      )
      val marshaller = new SAXModuleMarshaller(mFactory, mOptable)
      iModule.setComment(
        (if (getComment != null) getComment + "\n" else "") + "File generated: " + Calendar
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

  /*object ReadSystemFromXML {
    def apply(path: String): Option[Project] = {
      try {
        val fileUrl = new File(path).toURI.toURL
        return Some(new ProjectBuildFromXML().build(fileUrl))
      }
      catch {
        case t: Throwable => println(t)
      }
      None
    }
  }*/

  def getModule(module: ModuleSubject = mModule,
                hasProperties: Boolean = false): ProductDESProxy = {

    val expand = Config.EXPAND_EXTENDED_AUTOMATA.isTrue
    val factory = ProductDESElementFactory.getInstance
    val optimize = Config.OPTIMIZING_COMPILER.isTrue
    val compiler = new ModuleCompiler(mDocumentManager, factory, module)
    compiler.setOptimizationEnabled(optimize)
    compiler.setExpandingEFATransitions(expand)
    if (!hasProperties) {
      compiler.setEnabledPropositionNames(List.empty.asJavaCollection)
    }

    compiler.compile()
  }

  def verifyControllability(
    hypothesis: automata.Automaton,
    specs: Automata = getSupremicaSpecs
  ): (Boolean, String) = {
    info("Verifying.....")

    if (specs.nbrOfAutomata == 0) {
      throw new Exception("Spec is empty")
    } else {
      val sys = new Automata()
      sys.addAutomata(specs)
      sys.addAutomaton(hypothesis)

      val verificationOptions = new VerificationOptions()
      verificationOptions.setAlgorithmType(VerificationAlgorithm.MODULAR)
      verificationOptions.setVerificationType(VerificationType.CONTROLLABILITY)

      val verifier = new AutomataVerifier(
        sys,
        verificationOptions,
        new SynchronizationOptions(),
        new MinimizationOptions()
      )
      (verifier.verify(), verifier.getTheMessage)
    }
  }
}

//To create a new Module Subject....
trait SimpleModuleFactory {
  def moduleFactory(iModuleName: String,
                    iModuleComment: Option[String] = None): ModuleSubject = {
    val ms = new ModuleSubject(iModuleName, null)
    iModuleComment match {
      case Some(comment) => ms.setComment(comment)
      case _             =>
    }
    ms
  }

  def initModule(ms: ModuleSubject): ModuleSubject = { //Add marking to module
    ms.getEventDeclListModifiable.add(
      new ModuleSubjectFactory().createEventDeclProxy(
        new ModuleSubjectFactory()
          .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME),
        EventKind.PROPOSITION
      )
    )
    ms
  }

}
object SimpleModuleFactory extends SimpleModuleFactory {
  def apply(iModuleName: String,
            iModuleComment: Option[String] = None): ModuleSubject =
    initModule(moduleFactory(iModuleName, iModuleComment))
}
