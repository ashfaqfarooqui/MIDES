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

import grizzled.slf4j.Logging
import modelbuilding.core
import modelbuilding.core.{Alphabet, State, Transition}
import org.supremica.automata
import org.supremica.automata.IO.AutomataToXML
import org.supremica.automata._
import org.supremica.automata.algorithms._
import org.supremica.automata.LabeledEvent

import scala.collection.JavaConverters._
import scala.collection.immutable.Queue
import net.sourceforge.waters.model.des.EventProxy
import net.sourceforge.waters.subject.module.ModuleSubject

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

  def automatonToEFA(mModule: ModuleSubject, aut: Automaton) = {

    import org.supremica.automata.ExtendedAutomaton

    val efa = new ExtendedAutomaton(aut.getName(), aut.getKind())

    aut.getEvents.asScala.foreach(x => efa.addEvent(x.getName(), x.getKind().toString()))
    aut.getStates().asScala.foreach(x => efa.addState(x.getName()))

  }
  def exportAsSupremicaAutomata(aut: core.Automata, name: String = "Untitled"): Unit = {

    import modelbuilding.helpers.ConfigHelper
    val directoryName = ConfigHelper.outputDirectory
    val fileName      = s"$name.xml"
    val supAut        = new automata.Automata()
    aut.modules.foreach(a => supAut.addAutomaton(createSupremicaAutomaton(a)))
    // val fileName = s"$directoryName" +File.seperator+s"$name.xml"
    if (saveToXMLFile(directoryName, fileName, supAut))
      println(
        s"Exported automata to Supremica XML, file:${directoryName + File.separator + fileName}"
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
