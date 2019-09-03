package modelbuilding.core.modeling

import grizzled.slf4j.Logging
import modelbuilding.core.{Alphabet, Grammar, StateMap, StateMapTransition, StateSet, Uncontrollable, tau}
import org.supremica.automata
import org.supremica.automata.algorithms.AutomataSynchronizer.synchronizeAutomata
import org.supremica.automata.algorithms.Plantifier
import supremicastuff.SupremicaWatersSystem

import scala.collection.JavaConverters._

trait Specifications extends Logging {

  val specFilePath: Option[String]
  val syncSpecName:String = "SyncSpec"
  private var supremicaSpecs: Map[String, automata.Automaton] = Map.empty[String, automata.Automaton]
  private var supremicaAlphabet: automata.Alphabet = new automata.Alphabet()
  def getSupremicaSpecs: Map[String, automata.Automaton] = supremicaSpecs

  def addSynchronizedSpec:Unit={
    val specAutomata = new automata.Automata()
    supremicaSpecs.values.foreach(specAutomata.addAutomaton)
    val spec = synchronizeAutomata(specAutomata)
    spec.setName(syncSpecName)
    supremicaSpecs+=(spec.getName->spec)
  }

  def usePlantifiedSpec={
    val specAutomata = new automata.Automata()
    supremicaSpecs.values.foreach(specAutomata.addAutomaton)
    specAutomata.forEach(a=>Plantifier.plantify(a,supremicaAlphabet.getUncontrollableAlphabet))
    supremicaSpecs=Map.empty[String,automata.Automaton]
    supremicaSpecs=specAutomata.asScala.map(a=>s"${a.getName.replace(":","")}"->a).toMap
    info(s"plantified spec ${supremicaSpecs}")

  }

  def addSpecsFromSupremica(fileName: String): Unit = {
    supremicaSpecs = SupremicaWatersSystem(fileName).getSupremicaSpecs.asScala.map(a => s"${a.getName}" -> a).toMap
    supremicaSpecs.foreach { case (name, aut) =>
      assert(aut.isDeterministic, s"Spec `$name` is non-deterministic.")
    }
    supremicaAlphabet=SupremicaWatersSystem(fileName).getSupremicaAutomata.getUnionAlphabet
  }

  def isAccepting(spec: String, state: String): Boolean = supremicaSpecs(spec).getStateSet.getState(state).isAccepting

  def extendStateMap(stateMap: StateMap, specs: Set[automata.Automaton] = supremicaSpecs.values.toSet): StateMap = {
    StateMap(states = stateMap.states, specs = specs.map(s => s.getName -> s.getInitialState.getName).toMap)
  }





  def evalTransition(t: StateMapTransition, specs: Set[String]): Map[String, Option[String]] = {

    println("""##########""")
    println(t)
    println(specs)

    val sourceStates = specs.map(s => s -> t.source.specs(s)).toMap

    val targetStates: Map[String, Option[String]] = sourceStates.map { case (spec, sourceState) =>
      if (!supremicaSpecs(spec).getAlphabet.contains(t.event.getCommand.toString))
        spec -> Some(sourceState)
      else {
        val transitions = supremicaSpecs(spec).getStateSet.getState(sourceState).getOutgoingArcs.asScala.filter(_.getEvent.equals(t.event.getCommand.toString))
        if (transitions.isEmpty) spec -> None
        // else if (transitions.size > 1) throw new Error("") Can never occur since we have verified that the spec is deterministic
        else spec -> Some(transitions.head.getTarget.getName)
      }
    }.toMap

    targetStates
  }

}
