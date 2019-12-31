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

/*
Second version without partial states.

A brute force BFS that split the result into modules rather than a monolithic plant.

 */

package modelbuilding.solvers

import modelbuilding.core
import modelbuilding.core.interfaces.modeling.{ModularModel, Module}
import modelbuilding.core.{SUL, _}
import modelbuilding.solvers.FrehageModularSupSynthesis._
import org.supremica.automata

import scala.collection.mutable

object FrehageModularSupSynthesis {

  def getReducedStateMap(state: StateMap, module: Module): StateMap =
    StateMap(
      name = state.name,
      states = state.states.filterKeys(s => module.stateSet.states.contains(s)),
      specs = state.specs.filterKeys(s => module.specs.contains(s))
    )

  def getReducedStateMapTransition(
      t: StateMapTransition,
      module: Module
    ): StateMapTransition =
    StateMapTransition(
      getReducedStateMap(t.source, module),
      getReducedStateMap(t.target, module),
      if (module.alphabet.events contains t.event) t.event else Symbol(tau)
    )

}

class FrehageModularSupSynthesis(_sul: SUL) extends BaseSolver {

  val _model = _sul.model

  assert(
    _sul.specification.isDefined,
    "modelbuilder.solver.ModularSupSolver requires a specification."
  )
  assert(
    _model.isModular,
    "modelbuilder.solver.FrehageModularSupSynthesis requires a modular model."
  )

  private val specifications = _sul.specification.get
  private val model          = _model.asInstanceOf[ModularModel]

  //private val simulator: SUL = model.simulation

  def getSupervisorModules(
      supremicaSpecs: Map[String, automata.Automaton]
    ): Set[Module] = {

    if (!model.isModular) Set(Module(model.name, model.states, model.alphabet))
    else {

      val modularModel = model.asInstanceOf[ModularModel]

      if (supremicaSpecs.isEmpty) // If no Automata specs exist the original modules can be used.
        modularModel.modules.map(
          m => Module(m, modularModel.stateMapping(m), modularModel.eventMapping(m))
        )
      else {

        val specAlphabets: Map[String, Alphabet] = supremicaSpecs.map(
          s =>
            s._1 -> new Alphabet(
              model.alphabet.events
                .filter(e => s._2.getAlphabet.contains(e.getCommand.toString))
            )
        )

        def getModuleAlphabets(spec: String, acc: Alphabet): Alphabet = {
          val nextModules = modularModel.eventMapping.filter {
            case (m, a) =>
              acc.events.exists(e => !e.isControllable && a.events.contains(e))
          }.keys
          println(spec, nextModules)
          val next = new Alphabet(
            nextModules.flatMap(m => modularModel.eventMapping(m).events).toSet
          )

          // Iterate the alphabet to increase the scope
          //if (next.events subsetOf acc.events) acc else getModuleAlphabets(spec, acc + next)

          // Only perform one iteration, including modules that shares uc events directly with the spec.
          acc + next
        }
        val moduleEventMapping: Map[String, Alphabet] = specAlphabets.map(
          s =>
            s._1
              -> getModuleAlphabets(s._1, s._2)
        )

//        val moduleStateMapping: Map[String, StateSet] = moduleEventMapping.mapValues{ a1 =>
        val moduleStateMapping: Map[String, StateSet] = specAlphabets.mapValues { a1 =>
          StateSet(
            modularModel.eventMapping
              .filter {
                case (m, a2) =>
                  a1.events.exists(e => a2.events.contains(e))
              }
              .flatMap(x => modularModel.stateMapping(x._1).states)
              .toSet
          )
        }

        supremicaSpecs.keySet.map { s =>
          Module(
            name = s,
            stateSet = moduleStateMapping(s),
            alphabet = moduleEventMapping(s),
            Set(s)
          )
        }

      }
    }
  }

  def processUncontrollableState(m: Module, state: StateMap): Unit = {
    if (!moduleForbidden(m.name).contains(getReducedStateMap(state, m))) {
      moduleForbidden(m.name) += getReducedStateMap(state, m)
      for (t <- moduleTransitions(m.name)
           if t.target == state && !t.event.isControllable) {
        processUncontrollableState(m, t.source)
      }
    }
  }

  private val specModules: Set[Module] = getSupervisorModules(
    specifications.getSupremicaSpecs
  )
  private val remainingPlantModules: Set[String] = model.modules.filterNot(
    m => specModules.exists(s => model.eventMapping(m).events.subsetOf(s.alphabet.events))
  )
  private val plantModules: Set[Module] = remainingPlantModules.map(
    m => Module(m, model.stateMapping(m), model.eventMapping(m))
  )
//  private val plantModules: Set[Module] = Set.empty[Module]
  private val modules: Set[Module] = specModules ++ plantModules

  specModules foreach println

  private val initState: StateMap = specifications.extendStateMap(_sul.getInitState)

  private var queue: List[StateMap]          = List(initState)
  private val history: mutable.Set[StateMap] = mutable.Set(initState)

  private val moduleStates: Map[String, mutable.Set[StateMap]] =
    modules.map(m => m.name -> mutable.Set(getReducedStateMap(initState, m))).toMap
  private val moduleForbidden: Map[String, mutable.Set[StateMap]] =
    modules.map(m => m.name -> mutable.Set.empty[StateMap]).toMap
  private val moduleTransitions: Map[String, mutable.Set[StateMapTransition]] =
    modules.map(_.name -> mutable.Set.empty[StateMapTransition]).toMap

  var count = 0
  // Loop until all modules are done exploring new states
  while ({
    count += 1
    if (count % 1000 == 0) println(s"Iteration: $count, queue size: ${queue.size}")
    queue.nonEmpty
  }) {

    val state = queue.head
    queue = queue.tail

    val outgoingTransitionsInPlant: List[StateMapTransition] =
      _sul.getOutgoingTransitions(state, model.alphabet)

    for (t <- outgoingTransitionsInPlant) {

      // filter out those specifications that still is part of the state
      val remainingSpecifications = modules.filter(m => state.specs.contains(m.name))

      // Find the target states of the transitions in the specifications, caused by executing the event in current state
      val specTargetStates =
        specifications.evalTransition(t, remainingSpecifications.map(_.name))

      // if event is uncontrollable, those specifications that try to block it should be forbidden
      if (!t.event.isControllable) {
        remainingSpecifications.filter(m => specTargetStates(m.name).isEmpty).foreach {
          m =>
            processUncontrollableState(m, state)
          // TODO: Can we also forbid the state in all other modules that might shares the same transition?
        }
      }

      // filter out modules that should include this transitions, being those that (1 AND (2 OR 3)):
      // 1) does not have the transition blocked by the spec, i.e. their name remains in specTargetStates
      // 2) the state of the spec is changed by the transition
      // 3) at least one state variable is changed by the transition
      val filteredSpecs = remainingSpecifications.filter { m =>
        specTargetStates(m.name).nonEmpty
      }

      // Combine the remaining specification modules with the plant modules
      val remainingModules = filteredSpecs ++ plantModules

      // check what states have changed in transitions `t`
      val changedStates =
        t.target.states.keySet.filter(k => t.source.states(k) != t.target.states(k))

      // update the transition with the target states of the specifications
      val trans = StateMapTransition(
        t.source,
        StateMap(
          states = t.target.states,
          specs = specTargetStates.filter(_._2.nonEmpty).mapValues(_.get)
        ),
        t.event
      )

      // filter out modules that should include this transitions, being those where
      // the state of the spec OR where at least one state variable is changed by the transition.
      val concernedModules = remainingModules.filter { m =>
        state.specs.getOrElse(m.name, "-1") != specTargetStates
          .getOrElse(m.name, None)
          .getOrElse("") ||
        m.stateSet.states.intersect(changedStates).nonEmpty ||
        m.alphabet.events.contains(t.event)
      }

      var newStateFound = false
      for (m <- concernedModules) {
        if (plantModules.contains(m) || specTargetStates(m.name).nonEmpty) {
          val tReduced = getReducedStateMapTransition(trans, m)
          moduleTransitions(m.name) += tReduced
          if (!moduleStates(m.name).contains(tReduced.target)) {
            moduleStates(m.name) += tReduced.target
            newStateFound = true
          } else if (moduleForbidden(m.name).contains(tReduced.target) && !t.event.isControllable) {
            processUncontrollableState(m, tReduced.source)
          }
        }
      }
      if (newStateFound) queue = trans.target :: queue
    }
  }

  /*
   * PROCESS THE OUTPUT
   * 1) Identify all `Non Local Variables`
   * 2) Remove `Non Local Variables` from states and transitions
   * 3) Remove all uncontrollable states and connected transitions
   */
  private var reducedModuleInitStates: Map[String, StateMap] = Map.empty[String, StateMap]
  private val reducedModuleStates: Map[String, mutable.Set[StateMap]] =
    modules.map(m => m.name -> mutable.Set.empty[StateMap]).toMap
  private val reducedModuleForbidden: Map[String, mutable.Set[StateMap]] =
    modules.map(m => m.name -> mutable.Set.empty[StateMap]).toMap
  private val reducedModuleTransitions: Map[String, mutable.Set[StateMapTransition]] =
    modules.map(_.name -> mutable.Set.empty[StateMapTransition]).toMap

  for (m <- modules) {
    // 1
    val nonLocalVariables =
      moduleTransitions(m.name)
        .filter(_.event.getCommand == tau)
        .flatMap(
          t => t.source.states.keys.filter(k => t.source.states(k) != t.target.states(k))
        )
    // IF YOU WANT TO HARDCODE A NON LOCAL VARIABLE .union(if (m.name == "Zone42") Set("w1_5") else Set.empty[String])

    //println(m.name, nonLocalVariables)

    // 2
    def reduce(s: StateMap) =
      StateMap(s.states.filterKeys(k => !nonLocalVariables.contains(k)), s.specs)
    reducedModuleInitStates += (m.name -> reduce(getReducedStateMap(initState, m)))
    reducedModuleStates(m.name) ++= moduleStates(m.name).map(reduce)
    reducedModuleForbidden(m.name) ++= moduleForbidden(m.name).map(reduce)
    reducedModuleTransitions(m.name) ++= moduleTransitions(m.name)
      .map(t => StateMapTransition(reduce(t.source), reduce(t.target), t.event))

    // 3
    reducedModuleStates(m.name) --= reducedModuleForbidden(m.name)
    reducedModuleTransitions(m.name) --= reducedModuleTransitions(m.name).filter(
      t =>
        reducedModuleForbidden(m.name)
          .contains(t.source) || reducedModuleForbidden(m.name).contains(t.target)
    )
  }

  println("DONE!")
  println(s"Number of iterations: $count")

  override def getAutomata: Automata = {
    val automatons = for {
      m <- modules

      states: Map[StateMap, State] = reducedModuleStates(m.name)
        .map(s => {
          val name = ((if (s.states.forall { case (k, v) => initState.states(k) == v }
                           && s.specs.forall { case (k, v) => initState.specs(k) == v })
                         "INIT: "
                       else "")
            + s.toString)
          (s, State(name))
        })
        .toMap
      transitions: Set[Transition] = reducedModuleTransitions(m.name)
        .filterNot(_.event.getCommand == tau)
        .map(t => Transition(states(t.source), states(t.target), t.event))
        .toSet[Transition]
      alphabet: Alphabet = m.alphabet
      iState: State      = states(reducedModuleInitStates(m.name))
      fStates: Option[Set[State]] = Some(
        states
          .filter(
            s =>
              s._1.specs
                .forall(spec => specifications.isAccepting(spec._1, spec._2.toString))
          )
          .values
          .toSet
      )
      forbiddenStates = reducedModuleForbidden(m.name)
        .intersect(reducedModuleStates(m.name))
        .map(states(_)) match {
        case x if x.nonEmpty => Some(x.toSet)
        case _               => None
      }
    } yield Automaton(
      m.name,
      states.values.toSet,
      alphabet,
      transitions,
      iState,
      fStates,
      forbiddenStates
    )
    core.Automata(automatons)
  }

}
