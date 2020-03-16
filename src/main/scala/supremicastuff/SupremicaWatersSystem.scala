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

package supremicastuff

import java.io.File
import java.util.Calendar

import grizzled.slf4j.Logging
import net.sourceforge.waters.model.base.EventKind
import net.sourceforge.waters.model.compiler.{CompilerOperatorTable, ModuleCompiler}
import net.sourceforge.waters.model.des.ProductDESProxy
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.model.marshaller.{DocumentManager, SAXModuleMarshaller}
import net.sourceforge.waters.model.module.EventDeclProxy
import net.sourceforge.waters.plain.des.ProductDESElementFactory
import net.sourceforge.waters.subject.module.{ModuleSubject, ModuleSubjectFactory}
import org.supremica.automata
import org.supremica.automata.Automata
import org.supremica.automata.IO.ProjectBuildFromWaters
import org.supremica.automata.algorithms._
import org.supremica.automata.algorithms.minimization.MinimizationOptions
import org.supremica.properties.Config

import scala.collection.JavaConverters._
import net.sourceforge.waters.model.module.VariableComponentProxy
import net.sourceforge.waters.subject.module.SimpleComponentSubject
import scala.util.parsing.combinator.RegexParsers
import scala.io.Source

object ReadFromWmodFile {
  def apply(iFilePath: String): Option[ModuleSubject] = {

    try {
      val fileUri = new File(iFilePath).toURI
      val marshaller = new SAXModuleMarshaller(
        new ModuleSubjectFactory(),
        CompilerOperatorTable.getInstance()
      )
      Some(
        marshaller.unmarshal(fileUri).asInstanceOf[ModuleSubject]
      )
    } catch {
      case t: Throwable =>
        throw new IllegalArgumentException(
          s"Could not generate SupremicaWatersSystem from file `$iFilePath`, error: $t"
        )
    }
    None
  }
}
object SupremicaWatersSystem {
  def apply(iFilePath: String): SupremicaWatersSystem = {
    new SupremicaWatersSystem(ReadFromWmodFile(iFilePath).get)
  }
}

trait SupremicaBase {
  val mModule: ModuleSubject
  lazy val mFactory         = new ModuleSubjectFactory()
  lazy val mOptable         = CompilerOperatorTable.getInstance()
  lazy val mParser          = new ExpressionParser(mFactory, mOptable)
  lazy val mDocumentManager = new DocumentManager()

  def getAlphabet =
    mModule
      .getEventDeclListModifiable()
      .asScala
      .filter(!_.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME))
  def getVariables =
    mModule
      .getComponentListModifiable()
      .asScala
      .filter(_.isInstanceOf[VariableComponentProxy])
      .map(_.asInstanceOf[VariableComponentProxy])
  def getEFAs =
    mModule
      .getComponentListModifiable()
      .asScala
      .filter(_.isInstanceOf[SimpleComponentSubject])
      .map(_.asInstanceOf[SimpleComponentSubject])
  def getFlowers = getEFAs.filter(_.getGraph().getNodes().size() == 1)
  def getComment = if (mModule.getComment() == null) "" else s"${mModule.getComment()}\n"
}

class SupremicaWatersSystem(
    val mModule: ModuleSubject = SimpleModuleFactory("NewModule") /* e.g. ReadSystemFromWmodFile("supremicaFiles/controlabilitytest.xml") */)
    extends SimpleModuleFactory
    with SupremicaBase
    with Logging {

  assert(mModule != null, "The ModuleSubject must be non null")

  println("created sup model")
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

  def saveToWMODFile(iFilePath: String, iModule: ModuleSubject = mModule): Boolean = {
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

  def getModule(
      module: ModuleSubject = mModule,
      hasProperties: Boolean = false
    ): ProductDESProxy = {

    val expand   = Config.EXPAND_EXTENDED_AUTOMATA.isTrue
    val factory  = ProductDESElementFactory.getInstance
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
  def moduleFactory(
      iModuleName: String,
      iModuleComment: Option[String] = None
    ): ModuleSubject = {
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
  def apply(iModuleName: String, iModuleComment: Option[String] = None): ModuleSubject =
    initModule(moduleFactory(iModuleName, iModuleComment))
}
object TextFilePrefix extends TextFilePrefix
trait TextFilePrefix {
  lazy val COLON                 = ":"
  lazy val NAME                  = "\\w+"
  lazy val NAMES                 = s"\\{?((\\s*$NAME,)*\\s*$NAME\\s*)\\}?"
  lazy val VARIABLE_PREFIX       = "v" + COLON
  lazy val TRANSITION_PREFIX     = "t" + COLON
  lazy val TRANSITION_SET_PREFIX = "T" + COLON
  lazy val FORBIDDEN_PREFIX      = "x" + COLON
  lazy val OPERATION_PREFIX      = "o" + COLON
  lazy val COMMENT               = "//"
  lazy val UNCONTROLLABLE_PREFIX = "uc_"
  lazy val ALL                   = "(All-)"
}

trait ParseTextFileToModule
    extends SimpleModuleFactory
    with FlowerPopulater
    with RegexParsers
    with TextFilePrefix {
  var mValueStringIntMap: Map[String, String] = Map()
  def parseTextFileToModule(iTextFilePath: String): Boolean = {
    val lines = Source.fromFile(s"$iTextFilePath").getLines().toSeq

    //Variables-------------------------------------------------------------------------------------
    case class ParsedVariable(
        name: String,
        data: Option[Map[String, String]],
        comment: Option[String])
    lazy val dataTypes = Map("domain" -> "d", "init" -> "i", "marked" -> "m")
    def parseVariable(data: String) = {
      val updatedData = dataTypes.foldLeft(data.replaceAll(" |\t", "")) {
        case (updData, (full, short)) =>
          updData.replaceAll(s"$full$COLON", s"$short$COLON")
      }
      val typeRegex  = s"${dataTypes.values.mkString("(", "|", ")")}".r <~ s":".r
      val valueRegex = s"(.(?!:))+".r //As an example "named:" => "name"
      parseAll((typeRegex ~ valueRegex) *, updatedData) match {
        case Success(list, _) =>
          Some(list.map { case (~(name, data)) => name -> data }.toMap)
        case _ => None
      }
    }
    lazy val variables = lines.flatMap(
      str =>
        parseAll(
          (s"$VARIABLE_PREFIX".r ~> s"$NAME".r) ~ (s"[^/]+".r) ~ opt(
            s"$COMMENT".r ~> s".*".r
          ),
          str
        ) match {
          case Success(~(~(name, data), comment), _) =>
            Some(ParsedVariable(name, parseVariable(data), comment))
          case _ => None
        }
    )

    variables.foreach(v => {
      if (v.data.getOrElse(Map()).size != 3) {
        println(
          s"Problem with variable ${v.name}! One domain value, one init value, and one marked value are required!"
        ); return false
      }
      val rangeDomain = v.data.get("d").split("\\.\\.")
      if (rangeDomain.size == 2) { //must be a variables with integers
        val markedValues = v.data.get("m").split(",").map(Integer.parseInt).toSet
        if (!addVariable(
              v.name,
              Integer.parseInt(rangeDomain(0)),
              Integer.parseInt(rangeDomain(1)),
              Integer.parseInt(v.data.get("i")),
              markedValues
            ).isDefined) {
          return false
        }
      } else { //must be a variables with values
        val domain = v.data.get("d").split(",")
        if (!valuesFromStringToIntRepresentationFactory(domain)) {
          return false
        }
        val markedValues = v.data.get("m").split(",").map(mv => domain.indexOf(mv)).toSet
        if (!addVariable(
              v.name,
              0,
              domain.size - 1,
              domain.indexOf(v.data.get("i")),
              markedValues
            ).isDefined) {
          return false
        }
      }
      //Add variable values to module comment
      mModule.setComment(
        s"$getComment${TextFilePrefix.VARIABLE_PREFIX}${v.name} d${TextFilePrefix.COLON}${v.data.get("d")}"
      )
      if (v.comment.isDefined) mModule.setComment(s"$getComment${v.comment.get}")
    })

    //Transitions--------------------------------------------------------------------------------------
    case class ParsedTrans(
        name: String,
        data: (String, String),
        comment: Option[String]) { def guard = data._1; def action = data._2 }
    def parseGuardAction(data: String) = {
      data.replaceAll(("\\s|\\t"), "").split(s"c$COLON").foldLeft(("", "")) {
        case ((guard, action), condition) =>
          var localGuard = guard; var localAction = action
          if (!condition.isEmpty()) {
            val index = condition.indexOf(";")
            if (index != 0) {
              if (!guard.isEmpty()) {
                localGuard += "&"
              }
              if (index < 0) {
                localGuard += "(" + condition + ")"
              } else {
                localGuard += "(" + condition.substring(0, index) + ")"
              } // c:guard else c:guard;action(;action)*
            }
            if (index >= 0) {
              localAction += condition.substring(index)
            } // c:;action(;action)* || c:guard;action(;action)*
          }
          (localGuard, localAction)
      }
    }
    lazy val trans = lines
      .map(
        str =>
          parseAll(
            s"$TRANSITION_PREFIX".r ~> s"$NAME".r ~ s"[^/]+".r ~ opt(
              s"$COMMENT".r ~> s".*".r
            ),
            str
          ) match {
            case Success(~(~(name, data), comment), _) =>
              Some(ParsedTrans(name, parseGuardAction(data), comment))
            case _ => None
          }
      )
      .flatten

    trans.foreach { t =>
      addLeafAndEventToAlphabet(
        t.name,
        t.name.startsWith(UNCONTROLLABLE_PREFIX),
        valuesFromStringToIntRepresentation(t.guard),
        valuesFromStringToIntRepresentation(t.action)
      )
      if (t.comment.isDefined) mModule.setComment(s"$getComment${t.comment.get}")
    }

    //Transition sets-------------------------------------------------------------
    case class ParsedTransSet(
        names: Option[Seq[String]],
        data: (String, String),
        comment: Option[String]) { def guard = data._1; def action = data._2 }
    def parseTransSet(all: Option[String], names: String): Option[Seq[String]] = {
      val transFromNames = names.replaceAll(" ", "").split(",")
      transFromNames.filterNot(getAlphabet.map(_.getName()).contains).foreach { t =>
        println(s"The transition: $t has not been defined."); return None
      } // Are all transitions defined?
      Some(
        if (all.isDefined)
          getAlphabet.map(_.getName()).filterNot(transFromNames.contains).toSeq
        else transFromNames
      ) // Work with given transitions or complement
    }
    lazy val transSet = lines
      .map(
        str =>
          parseAll(
            s"$TRANSITION_SET_PREFIX".r ~> opt(s"$ALL".r) ~ s"$NAMES".r ~ s"[^/]+".r ~ opt(
              s"$COMMENT".r ~> s".*".r
            ),
            str
          ) match {
            case Success(~(~(~(all, names), data), comment), _) =>
              Some(
                ParsedTransSet(
                  parseTransSet(all, names.replaceAll("\\{|\\}", "")),
                  parseGuardAction(data),
                  comment
                )
              )
            case _ => None
          }
      )
      .flatten

    transSet.foreach { ts =>
      if (!ts.names.isDefined) {
        println("Problem to parse set of transitions"); return false
      }
      ts.names.get.foreach(
        t =>
          addLeaf(
            t,
            valuesFromStringToIntRepresentation(ts.guard),
            valuesFromStringToIntRepresentation(ts.action)
          )
      )
    }

    //Forbidden state combinations------------------------------------------------
    lazy val fscs = lines
      .map(
        str =>
          parseAll(
            s"$FORBIDDEN_PREFIX".r ~> s"$NAME".r ~ s"[^/]+".r ~ opt(
              s"$COMMENT".r ~> s".*".r
            ),
            str
          ) match {
            case Success(~(~(name, data), comment), _) => Some(data)
            case _                                     => None
          }
      )
      .flatten

//    fscs.foreach(d => addForbiddenExpression(valuesFromStringToIntRepresentation(d), addSelfLoop = false, addInComment = true))
    fscs.foreach(
      d => addForbiddenExpression(valuesFromStringToIntRepresentation(d), true, false)
    )

    //Parse was ok
    return true
  }

  private def valuesFromStringToIntRepresentationFactory(
      valueDomain: Seq[String]
    ): Boolean = {
    valueDomain
      .filter(mValueStringIntMap.contains)
      .filter(
        v => !mValueStringIntMap(v).equals(Integer.toString(valueDomain.indexOf(v)))
      )
      .foreach { v =>
        println(
          s"The value $v appears at two different positions in two different domains. For parsing simplicity, this is not allowed!"
        ); return false
      }
    valueDomain.filter(v => getVariables.map(_.getName()).contains(v)).foreach { v =>
      println(
        s"The value $v coincides with the name of a variable. For parsing simplicity, this is not allowed!"
      ); return false
    }
    mValueStringIntMap = valueDomain.foldLeft(mValueStringIntMap) {
      case (acc, v) => acc + (v -> Integer.toString(valueDomain.indexOf(v)))
    }
    return true
  }
  private def valuesFromStringToIntRepresentation(expr: String): String =
    mValueStringIntMap.foldLeft(expr) {
      case (acc, (k, v)) => acc.replaceAll(s"\\b$k\\b", v)
    }

}
