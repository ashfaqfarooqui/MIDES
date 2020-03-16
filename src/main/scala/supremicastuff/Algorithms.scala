package supremicastuff

import scala.collection.JavaConverters._
import org.supremica.automata.algorithms.EditorSynthesizerOptions
import org.supremica.automata.algorithms.SynthesisType
import org.supremica.automata.algorithms.SynthesisAlgorithm
import org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer
import org.supremica.automata.ExtendedAutomata
import net.sourceforge.waters.xsd.SchemaBase.ELEMENT_EventKind
import java.util.Vector
import net.sourceforge.waters.model.module.EnumSetExpressionProxy
import org.supremica.automata.IO.ProjectBuildFromWaters
import net.sourceforge.waters.model.marshaller.DocumentManager
import org.supremica.automata.Automata
import org.supremica.automata.Automaton
import org.supremica.automata.algorithms.SynthesizerOptions
import org.supremica.automata.algorithms.SynchronizationOptions
import org.supremica.automata.algorithms.AutomataSynthesizer
import org.supremica.automata.algorithms.SynchronizationType
import org.supremica.properties.Config
import org.supremica.automata.BDD.EFA.BDDExtendedAutomata
import org.supremica.util.ActionTimer
import org.supremica.automata.BDD.EFA._
import net.sourceforge.waters.model.module.SimpleExpressionProxy
import net.sourceforge.waters.model.expr.Operator
import org.supremica.automata.BDD.EFA.BDDExtendedAutomata
import org.supremica.util.BDD._
import scala.util.parsing.combinator.RegexParsers
import net.sourceforge.waters.model.base.EventKind

trait Algorithms extends SupremicaBase with RegexParsers {

 
  def getDFA: Option[Iterable[Automaton]] = {
    try {
      val proj = new ProjectBuildFromWaters(new DocumentManager())
      Config.OPTIMIZING_COMPILER.set(true)
      val automata = proj.build(mModule).asInstanceOf[Automata]
      return Some(automata.asScala)
    } catch {
      case t: Throwable => println(t); None
    }
  }

  def getAutomataSupervisor(
      synthesisType: SynthesisAlgorithm = SynthesisAlgorithm.MONOLITHIC
    ): Option[Iterable[Automaton]] = {
    getDFA match {
      case Some(as) => getAutomataSupervisor(as, synthesisType)
      case _        => None
    }
  }

  def getAutomataSupervisor(
      automatonIt: Iterable[Automaton],
      synthesisType: SynthesisAlgorithm
    ): Option[Iterable[Automaton]] = {
    val automata = new Automata()
    automatonIt.foreach(automata.addAutomaton)
    val syntho = new SynthesizerOptions()
    syntho.setSynthesisType(SynthesisType.NONBLOCKING_CONTROLLABLE)
    syntho.setSynthesisAlgorithm(synthesisType)
    syntho.setSynthesisAlgorithm(SynthesisAlgorithm.COMPOSITIONAL_WATERS)
    syntho.setPurge(true)
    val syncho = new SynchronizationOptions()
    syncho.setSynchronizationType(SynchronizationType.FULL)
    val as = new AutomataSynthesizer(automata, syncho, syntho)
    try {
      val supervisors = as.execute()
      return Some(supervisors.asScala)
    } catch {
      case e: Exception => println(e); None
    }
  }

}
