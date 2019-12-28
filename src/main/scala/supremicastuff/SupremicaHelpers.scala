package supremicastuff

import java.io.File

import grizzled.slf4j.Logging
import modelbuilding.core
import modelbuilding.core.{Alphabet, State, Transition}
import org.supremica.automata
import org.supremica.automata.IO.AutomataToXML
import org.supremica.automata._
import org.supremica.automata.algorithms._

object SupremicaHelpers extends Logging {

  def createSupremicaAutomaton(aut: core.Automaton): automata.Automaton =
    createSupremicaAutomaton(
      aut.states,
      aut.transitions,
      aut.alphabet,
      aut.iState,
      aut.fState,
      aut.forbiddenStates,
      aut.name
    )
  def createSupremicaAutomaton(
      states: Set[State],
      transitions: Set[Transition],
      A: Alphabet,
      iState: State,
      fState: Option[Set[State]],
      forbiddenState: Option[Set[State]],
      name: String = "hypothesis"
    ): automata.Automaton = {
    val aut = new org.supremica.automata.Automaton(name)
    states.foreach(s => aut.addState(new automata.State(s.s)))
    aut.setInitialState(aut.getStateWithName(iState.s))
    fState.foreach(_.foreach(s => aut.getStateWithName(s.s).setAccepting(true)))
    forbiddenState.foreach(_.foreach(s => aut.getStateWithName(s.s).setForbidden(true)))

    A.events
      .map { e =>
        val l = new LabeledEvent(e.toString())
        l.setControllable(e.isControllable)
        l
      }
      .foreach(aut.getAlphabet.addEvent(_))

    transitions.foreach { t =>
      aut.addArc(
        new Arc(
          aut.getStateWithName(t.source.s),
          aut.getStateWithName(t.target.s),
          aut.getAlphabet.getEvent(t.event.getCommand.toString)
        )
      )
    }
    aut.setType(AutomatonType.SUPERVISOR)
    aut
  }

  def exportAsSupremicaAutomata(aut: core.Automata, name: String = "Untitled"): Unit = {

    import Helpers.ConfigHelper
    val directoryName = ConfigHelper.outputDirectory
    val fileName      = s"$name.xml"
    val supAut        = new automata.Automata()
    aut.modules.foreach(a => supAut.addAutomaton(createSupremicaAutomaton(a)))
    // val fileName = s"$directoryName" +File.seperator+s"$name.xml"
    if (saveToXMLFile(directoryName, fileName, supAut))
      println(
        s"Exported automata to Supremica XML, file:$directoryName +${File.separator}+ $fileName"
      )
    else
      println("Failed to export automata to Supremica XML.")
  }

  def synchronize(a: automata.Automata): automata.Automaton = {
    AutomataSynchronizer.synchronizeAutomata(a)
  }

  /*object ReadProjectFromXML {
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

  def saveToXMLFile(
      iFilePath: String,
      fileName: String,
      aut: automata.Automata
    ): Boolean = {
    try {

      val directory = new File(iFilePath);
      if (!directory.exists()) {
        directory.mkdir();
        // If you require it to make the entire directory path including parents,
        debug("created output directory")
        // use directory.mkdirs(); here instead.
      }
      val file = new File(iFilePath + File.separator + fileName)
      new AutomataToXML(aut).serialize(file)

      return true
    } catch {
      case t: Throwable => println(t)
    }
    false
  }

}
