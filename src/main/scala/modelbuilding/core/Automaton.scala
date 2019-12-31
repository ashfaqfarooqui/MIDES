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

package modelbuilding.core

import java.io.{File, PrintWriter}

import grizzled.slf4j.Logging
import scalax.collection.Graph
import scalax.collection.edge.LkDiEdge
import scalax.collection.io.dot._ //{DotAttr, DotEdgeStmt, DotGraph, DotRootGraph, Id}

case class Automaton(
    name: String,
    states: Set[State],
    alphabet: Alphabet,
    transitions: Set[Transition],
    iState: State,
    fState: Option[Set[State]] = None,
    forbiddenStates: Option[Set[State]] = None)
    extends Logging {

  lazy val transitionFunction: Map[(State, Symbol), State] =
    transitions.map(t => (t.source, t.event) -> t.target).toMap

  def createGraph: Graph[String, LkDiEdge] = {

    val edges = transitions.map(t => LkDiEdge(t.source.s, t.target.s)(t.event.toString))
    Graph.from(states.map(_.s).toList, edges) //.filter(p => p.e._1.s != "s0" && p.e._2.s != "s0" ))

  }

  lazy val getGraphAsDot: String = {

    val root = {
      DotRootGraph(directed = true, id = Some(Id(name)))
    }

    def edgeTransformer(
        innerEdge: Graph[String, LkDiEdge]#EdgeT
      ): Option[(DotGraph, DotEdgeStmt)] =
      innerEdge.edge match {
        case LkDiEdge(source, target, label) =>
          label match {
            case label: String =>
              Some(
                (
                  root,
                  DotEdgeStmt(
                    source.toString,
                    target.toString,
                    if (label.nonEmpty) List(DotAttr(Id("label"), Id(label.toString)))
                    else Nil
                  )
                )
              )
          }
      }

    val gDot = {
      createGraph.toDot(root, edgeTransformer)
    }
    gDot

  }

  def createDotFile: Unit = {
    import modelbuilding.helpers.ConfigHelper
    val gDot          = getGraphAsDot
    val directoryName = ConfigHelper.outputDirectory
    val directory     = new File(directoryName);
    if (!directory.exists()) {
      directory.mkdir();
      // If you require it to make the entire directory path including parents,
      debug("created output directory")
      // use directory.mkdirs(); here instead.
    }
    val pw = new PrintWriter(
      new File(directoryName + File.separator + s"${name.replaceAll("\\s", "")}.dot")
    )
    pw.write(gDot)
    pw.close
    println(s"Graph saved to $name.dot")
  }

  def getNextStates(state: State): Option[Set[State]] = {
    val states = for (e <- alphabet.events) yield transitionFunction(state, e)
    if (states.nonEmpty) Some(states) else None
  }

  def getInitialState: State             = iState
  def getMarkedState: Option[Set[State]] = fState

  def removeDumpState = {
    Automaton(
      name,
      states.filterNot(_.s == "dump:"),
      alphabet,
      transitions.filterNot(t => t.target.s == "dump:" || t.source.s == "dump:"),
      iState,
      fState match {
        case Some(value) => Some(value.filterNot(_.s == "dump:"))
        case None        => None
      },
      forbiddenStates match {
        case Some(value) => Some(value.filterNot(_.s == "dump:"))
        case None        => None
      }
    )
  }

  def removeTauEvents = {
    Automaton(
      name,
      states,
      new Alphabet(alphabet.events - Symbol(tau)),
      transitions.filterNot(_.event.getCommand == tau),
      iState,
      fState,
      forbiddenStates
    )
  }
  def removeTauAndDump = {
    removeDumpState.removeTauEvents
  }

  override def toString: String = {

    s"Automaton( $name, " +
      s"Q: (${states.map(_.s).mkString(",")}), " +
      s"A: (${alphabet.events.map(_.getCommand.toString).mkString(",")}), " +
      s"T: (${transitions.map { case Transition(s, t, e) => s"(${s.s},${t.s},$e)" }.mkString(",")}) " +
      s"q_i: (${iState.s}) " +
      s"Q_m: ${fState match {
        case Some(fs) => "(" + fs.map(_.s).mkString(",") + ")"
        case None     => "None"
      }} " +
      s"Q_f: (${forbiddenStates match {
        case Some(fs) => "(" + fs.map(_.s).mkString(",") + ")"
        case None     => "None"
      }}) )" +
      s"\n" +
      s" Properties:\n" +
      s"${name}" +
      s"\n Number of Transitions: ${transitions.size}" +
      s"\n Number of States: ${states.size}" +
      s"\n Number of events: ${alphabet.events.size}" +
      s"\n Number of Forbidden States: ${forbiddenStates match {
        case Some(fs) => fs.size
        case None     => 0
      }}" +
      s"\n \n \n"

  }

}
