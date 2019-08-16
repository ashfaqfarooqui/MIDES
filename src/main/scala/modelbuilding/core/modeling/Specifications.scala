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
            m._2.events.filter(!_.isControllable).map(_.getCommand.toString).intersect(s._2.eventIterator.asScala.map(_.getLabel).toSet).nonEmpty
          ).keys
        )

        val specMap: Map[String,(StateSet,Alphabet)] = specBlocks.map{ case (spec,modules) =>
          spec -> modules.foldLeft((StateSet(): StateSet, specAlphabets(spec): Alphabet)) {
            (acc, m) => (acc._1 + modularModel.stateMapping(m), acc._2)
          }
        }

        specMap.map(s => Module(s._1, s._2._1, s._2._2, Set(s._1))).toSet
      }
    }
  }

  def extendStateMap(stateMap: StateMap, specs: Set[automata.Automaton] = supremicaSpecs.values.toSet): StateMap = {
    StateMap(states = stateMap.state, specs = specs.map(s => s.getName -> s.getInitialState.getName).toMap)
  }

  def evalTransition(t: StateMapTransition, specs: Set[String]): Map[String, Option[String]] = {

    val sourceStates = specs.map(s => s -> t.source.specs(s))

    val targetStates: Map[String, Option[String]] = sourceStates.map { case (spec, sourceState) =>
      if (!supremicaSpecs(spec).getAlphabet.contains(t.event.getCommand.toString))
        spec -> Some(sourceState)
      else {
        val transitions = supremicaSpecs(spec).getStateSet.getState(sourceState).getOutgoingArcs.asScala
        if (transitions.isEmpty) spec -> None
        // else if (transitions.size > 1) throw new Error("") Can never occur since we have verified that the spec is deterministic
        else spec -> Some(transitions.head.getTarget.getName)
      }
    }.toMap

    targetStates
  }



  /*
   * Evaluates a transition
   */
  /*def getOutgoingTransitionsInSpecs(stateMap: StateMap, commands: Alphabet = alphabet): Map[Symbol, Map[String,String]] = {

    val individualOutgoingArcs = stateMap.specs.flatMap { s =>
      supremicaSpecs(s._1).getStateSet.getState(s._2.toString).getOutgoingArcs.asScala.map(t => (t.getLabel, s._1, t.getTarget.getName)).toSet.
        union(for (e <- commands.events.map(_.getCommand.toString) if !supremicaSpecs(s._1).getAlphabet.contains(e)) yield (e, s._1, s._2))
    }
//    individualOutgoingArcs.foreach(x=>println("#" + x))

    val groupByEvent = individualOutgoingArcs.groupBy(_._1).mapValues(_.map(x => (x._2, x._3)).toMap)
//    groupByEvent.foreach(x=>println("#%" + x))

//    val filterEvents: Map[String, Map[String,String]] = groupByEvent.filter(_._2.size == stateMap.specs.size)
//    filterEvents.foreach(x=>println("#%%" + x))

    val convertEventsToSymbols: Map[Symbol, Map[String,String]] =
      (for (e <- commands.events if groupByEvent contains e.getCommand.toString)
      yield e -> groupByEvent(e.getCommand.toString)).toMap

//    convertEventsToSymbols foreach println

    convertEventsToSymbols

  }*/

}
