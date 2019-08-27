package modelbuilding.core.modeling

import modelbuilding.core.{Alphabet, StateMap, StateMapTransition, StateSet}
import org.supremica.automata
import supremicastuff.SupremicaWatersSystem

import scala.collection.JavaConverters._

trait Specifications extends Model {

  override val hasSpecs: Boolean = true

  private var supremicaSpecs: Map[String, automata.Automaton] = Map.empty[String, automata.Automaton]

  def getSupremicaSpecs: Map[String, automata.Automaton] = supremicaSpecs

  def addSpecsFromSupremica(fileName: String): Unit = {
    supremicaSpecs = SupremicaWatersSystem(fileName).getSupremicaSpecs.asScala.map(a => s"${a.getName}" -> a).toMap
    supremicaSpecs.foreach { case (name, aut) =>
      assert(aut.isDeterministic, s"Spec `$name` is non-deterministic.")
    }
  }

  def getSupervisorModules: Set[Module] = {

    if (!this.isModular) Set(Module(this.name, states, alphabet))
    else {

      val modularModel = this.asInstanceOf[ModularModel]

      if (supremicaSpecs.isEmpty) // If no Automata specs exist the original modules can be used.
        modularModel.modules.map(m => Module(m, modularModel.stateMapping(m), modularModel.eventMapping(m)))
      else {

        val specAlphabets: Map[String, Alphabet] = supremicaSpecs.map(s => s._1 -> new Alphabet(alphabet.events.filter(e => s._2.getAlphabet.contains(e.getCommand.toString))))

        val specBlocks = supremicaSpecs.map(s =>
          s._1 -> modularModel.eventMapping.filter(m =>
            m._2.events.map(_.getCommand.toString).intersect(s._2.eventIterator.asScala.map(_.getLabel).toSet).nonEmpty
          ).keys
        )

        supremicaSpecs.keySet.map{ s =>
          Module(
            name = s,
            stateSet = StateSet(
              modularModel.eventMapping.filter{ case(m,a) =>
                specAlphabets(s).events.exists(e => a.events.contains(e))
              }.flatMap(x => modularModel.stateMapping(x._1).states).toSet
            ),
            alphabet = specAlphabets(s) + new Alphabet(
              modularModel.eventMapping.filter{ case(m,a) =>
                specAlphabets(s).events.exists(e => !e.isControllable && a.events.contains(e))
              }.flatMap(_._2.events).toSet
            ),
            Set(s)
          )
        }

      }
    }
  }

  def isAccepting(spec: String, state: String): Boolean = supremicaSpecs(spec).getStateSet.getState(state).isAccepting

  def extendStateMap(stateMap: StateMap, specs: Set[automata.Automaton] = supremicaSpecs.values.toSet): StateMap = {
    StateMap(states = stateMap.states, specs = specs.map(s => s.getName -> s.getInitialState.getName).toMap)
  }

  def evalTransition(t: StateMapTransition, specs: Set[String]): Map[String, Option[String]] = {

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
