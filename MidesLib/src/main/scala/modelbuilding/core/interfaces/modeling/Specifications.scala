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

package modelbuilding.core.interfaces.modeling

import grizzled.slf4j.Logging
import modelbuilding.core.{StateMap, StateMapTransition}
import org.supremica.automata
import org.supremica.automata.algorithms.AutomataSynchronizer.synchronizeAutomata
import org.supremica.automata.algorithms.Plantifier
import supremicastuff.SupremicaWatersSystem

import scala.collection.JavaConverters._

trait Specifications extends Logging {

  val specFilePath: Option[String]
  val syncSpecName: String = "SyncSpec"
  private var supremicaSpecs: Map[String, automata.Automaton] =
    Map.empty[String, automata.Automaton]
  private var supremicaAlphabet: automata.Alphabet       = new automata.Alphabet()
  def getSupremicaSpecs: Map[String, automata.Automaton] = supremicaSpecs

  def addSynchronizedSpec: Unit = {
    val specAutomata = new automata.Automata()
    supremicaSpecs.values.foreach(specAutomata.addAutomaton)
    val spec = synchronizeAutomata(specAutomata)
    spec.setName(syncSpecName)
    supremicaSpecs += (spec.getName -> spec)
  }

  def usePlantifiedSpec = {
    val specAutomata = new automata.Automata()
    supremicaSpecs.values.foreach(specAutomata.addAutomaton)
    specAutomata.forEach(a =>
      Plantifier.plantify(a, supremicaAlphabet.getUncontrollableAlphabet)
    )
    supremicaSpecs = Map.empty[String, automata.Automaton]
    supremicaSpecs =
      specAutomata.asScala.map(a => s"${a.getName.replace(":", "")}" -> a).toMap
    info(s"plantified spec ${supremicaSpecs}")

  }

  def addSpecsFromSupremica(fileName: String): Unit = {
    supremicaSpecs = SupremicaWatersSystem(fileName).getSupremicaSpecs.asScala
      .map(a => s"${a.getName}" -> a)
      .toMap
    supremicaSpecs.foreach { case (name, aut) =>
      assert(aut.isDeterministic, s"Spec `$name` is non-deterministic.")
    }
    supremicaAlphabet = SupremicaWatersSystem(
      fileName
    ).getSupremicaAutomata.getUnionAlphabet
  }

  def isAccepting(spec: String, state: String): Boolean =
    supremicaSpecs(spec).getStateSet.getState(state).isAccepting

  def extendStateMap(
      stateMap: StateMap,
      specs: Set[automata.Automaton] = supremicaSpecs.values.toSet
    ): StateMap = {
    StateMap(
      states = stateMap.states,
      specs = specs.map(s => s.getName -> s.getInitialState.getName).toMap
    )
  }

  def evalTransition(
      t: StateMapTransition,
      specs: Set[String]
    ): Map[String, Option[String]] = {

    val sourceStates = specs.map(s => s -> t.source.specs(s)).toMap

    val targetStates: Map[String, Option[String]] = sourceStates.map {
      case (spec, sourceState) =>
        if (!supremicaSpecs(spec).getAlphabet.contains(t.event.getCommand.toString))
          spec -> Some(sourceState)
        else {
          val transitions = supremicaSpecs(spec).getStateSet
            .getState(sourceState)
            .getOutgoingArcs
            .asScala
            .filter(_.getEvent.equals(t.event.getCommand.toString))
          if (transitions.isEmpty) spec -> None
          // else if (transitions.size > 1) throw new Error("") Can never occur since we have verified that the spec is deterministic
          else spec -> Some(transitions.head.getTarget.getName)
        }
    }.toMap

    targetStates
  }

}
