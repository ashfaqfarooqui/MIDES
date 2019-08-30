package modelbuilding.core.modeling

import modelbuilding.core.{Alphabet, Grammar, StateMap, StateMapTransition, StateSet, Uncontrollable, tau}
import org.supremica.automata
import org.supremica.automata.{Automaton, State}
import supremicastuff.SupremicaWatersSystem

import scala.collection.JavaConverters._

trait Specifications {

  val specFilePath: Option[String]
  private var supremicaSpecs: Map[String, automata.Automaton] = Map.empty[String, automata.Automaton]

  def getSupremicaSpecs: Map[String, automata.Automaton] = supremicaSpecs

  def addSpecsFromSupremica(fileName: String): Unit = {
    supremicaSpecs = SupremicaWatersSystem(fileName).getSupremicaSpecs.asScala.map(a => s"${a.getName}" -> a).toMap
    supremicaSpecs.foreach { case (name, aut) =>
      assert(aut.isDeterministic, s"Spec `$name` is non-deterministic.")
    }
  }

  def isAccepting(spec: String, state: String): Boolean = supremicaSpecs(spec).getStateSet.getState(state).isAccepting

  def extendStateMap(stateMap: StateMap, specs: Set[automata.Automaton] = supremicaSpecs.values.toSet): StateMap = {
    StateMap(states = stateMap.states, specs = specs.map(s => s.getName -> s.getInitialState.getName).toMap)
  }

  //Check-> send in a function to evaluate additionally. here I use if to check accepting nature of the reached state
  def isSequenceAllowedInSpec(grammar: Grammar, spec: String,check: State=>Boolean):Boolean={

    assert(supremicaSpecs.keySet.contains(spec), "spec should exist")
    val automaton = supremicaSpecs(spec)
    val s=automaton.getInitialState
    val alphabet=automaton.getAlphabet
    val p=grammar.getSequenceAsString.filterNot(_==tau.toString).filter(a=>alphabet.contains(a.toString))

    def loop(s:State,p:List[String]):Boolean={
      //debug(s"inloop: at $s and ${p}")
      //debug(s"check ${check(s)}")
      if(p.nonEmpty && s.getOutgoingArcs.asScala.exists(_.getEvent.getLabel==p.head)){
        loop(s.getOutgoingArcs.asScala.find(_.getEvent.getLabel==p.head).get.getToState,p.tail)
      }
      else if(p.isEmpty && check(s)) true else {false}
    }
    loop(s,p)
  }

  def isSequenceControllableInSpec(sequence:Grammar, alphabet: Alphabet,specName:String):Int={

    assert(supremicaSpecs.keySet.contains(specName), "spec should exist")
    val spec = supremicaSpecs(specName)
    val pref = sequence.getAllPrefixes
    val ucEvents = alphabet.events.filter(_.getCommand.isInstanceOf[Uncontrollable])
    val su = ucEvents //.flatMap(a=>ucEvents.map(a+_)).flatMap(a=>ucEvents.map(a+_))
    val tu = pref.flatMap(t=>su.map(t+_))
    //val tuNotCtrl = tu.filterNot(x=>hasValidPath(x,spec,_=>true)).exists(runCmdOnSULWithoutGoal(_))
 //   if(tuNotCtrl) {
    if(true) {
      0
    }
    else 2
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
