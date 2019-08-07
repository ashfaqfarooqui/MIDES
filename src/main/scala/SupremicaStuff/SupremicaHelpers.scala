package SupremicaStuff

import java.io.File
import java.net.URL
import java.util.Calendar

import org.supremica.properties.Config
import modelbuilding.core.{Alphabets, Controllable, State, StateMap, Symbol, Uncontrollable}
import grizzled.slf4j.Logging
import net.sourceforge.waters.analysis.monolithic.MonolithicNerodeEChecker
import net.sourceforge.waters.model.compiler.{CompilerOperatorTable, ModuleCompiler}
import net.sourceforge.waters.model.marshaller.{DocumentManager, JAXBModuleMarshaller}
import net.sourceforge.waters.subject.module.{ModuleSubject, ModuleSubjectFactory}
import org.supremica.automata
import org.supremica.automata.IO.{AutomataToWaters, AutomataToXML, ProjectBuildFromWaters, ProjectBuildFromXML}
import org.supremica.automata._
import org.supremica.automata.algorithms.minimization.MinimizationOptions
import org.supremica.automata.algorithms._
import net.sourceforge.waters.subject.module.ModuleSubject
import net.sourceforge.waters.subject.module.ModuleSubjectFactory
import net.sourceforge.waters.model.compiler.CompilerOperatorTable
import net.sourceforge.waters.model.des.{AutomatonTools, ProductDESProxyFactory}
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.model.module.EventDeclProxy
import net.sourceforge.waters.xsd.base.EventKind
import net.sourceforge.waters.model.module.VariableComponentProxy
import net.sourceforge.waters.subject.module.SimpleComponentSubject

import scala.collection.JavaConverters._
import net.sourceforge.waters.model.module.EnumSetExpressionProxy
import net.sourceforge.waters.plain.des.ProductDESElementFactory



trait SupremicaHelpers extends Base with Logging {

  //With StateMap
  def createSupremicaAutomaton(states: Set[State], transition: (State, Symbol) => State, A: Alphabets, iState: State, fState: Option[Set[State]], forbiddenState:Option[Set[State]],name:String ="hypothesis"): automata.Automaton = {
    val aut = new org.supremica.automata.Automaton()
    states.foreach(s => aut.addState(new automata.State(s.s)))
    aut.setInitialState(aut.getStateWithName(iState.s))
    fState.foreach(_.foreach(s => aut.getStateWithName(s.s).setAccepting(true)))
    forbiddenState.foreach(_.foreach(s=>aut.getStateWithName(s.s).setForbidden(true)))
    def isControllable(e: Symbol) = {
      e.getCommand match {
        case a:Controllable => true
        case a:Uncontrollable => false
      }
    }

    A.a.filterNot(_.getCommand.toString=="tou").map{ e=>
      val l=new LabeledEvent(e.toString())
    l.setControllable(isControllable(e))
      l
    }.foreach(aut.getAlphabet.addEvent(_))

    /*A.a.foreach(e=>aut.getAlphabet.addEvent(e.toString match {
      case "tou" => new automata.TauEvent(new LabeledEvent(e.toString))
      case _ => new LabeledEvent(e.toString)
    } ))*/
    for {
      s <- states
      a <- A.a.filterNot(_.getCommand.toString=="tou")

    } {
      aut.addArc(new Arc(aut.getStateWithName(s.s), aut.getStateWithName(transition(s, a).s), aut.getAlphabet.getEvent(a.s.toString)))
    }
    aut.removeState(aut.getStateWithName("dump:"))
    aut.setName(name)
    aut.setType(AutomatonType.SUPERVISOR)
    aut

  }

  def createWatersAutomataFromSupremica(aut:Automata)={

  }

  def synchronize(a:Automata)= {
    AutomataSynchronizer.synchronizeAutomata(a)
  }

  def getSupremicaAutomataFromWaters(moduleSubject: ModuleSubject = mModule): Option[Iterable[Automaton]] = {
    try {
      val proj = new ProjectBuildFromWaters(new DocumentManager())
      //Config.OPTIMIZING_COMPILER.set(true)
      val automata = proj.build(moduleSubject).asInstanceOf[Automata]
      return Some(automata.asScala)
    } catch {
      case t: Throwable => println(t); None
    }
  }

  lazy val getSupremicaSpecFromAutomata = {
    val a = new Automata()
    //getWatersModule(mModule).getAutomata.iterator().asScala.filter(_.getKind=="SPEC").asInstanceOf[Iterable[Automaton]].foreach(a.addAutomaton(_))
    getSupremicaAutomataFromWaters().get.filter(_.isSpecification).foreach(a.addAutomaton) // filter(_.getKind == "SPEC")
    a
  }


  def verifyControllability(hypothesis: automata.Automaton, spec: Automata = getSupremicaSpecFromAutomata) = {
    info("Verifying.....")

    if (spec == None) {
      throw new Exception("Spec is null")
    } else {


      val sys = new Automata()
      sys.addAutomata(spec)
      sys.addAutomaton(hypothesis)

      val verificationOptions = new VerificationOptions()
      verificationOptions.setAlgorithmType(VerificationAlgorithm.MODULAR)
      verificationOptions.setVerificationType(VerificationType.CONTROLLABILITY)

      val verifier = new AutomataVerifier(sys, verificationOptions, new SynchronizationOptions(), new MinimizationOptions())
      (verifier.verify(), verifier.getTheMessage)

    }
  }





}

