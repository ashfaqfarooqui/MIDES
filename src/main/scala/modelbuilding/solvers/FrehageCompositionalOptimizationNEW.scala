/*

Second version without partial states.

A brute force BFS that split the result into modules rather than a monolithic plant.

 */

package modelbuilding.solvers

import modelbuilding.core
import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core.interfaces.simulator.TimedCodeSimulator
import modelbuilding.core.{SUL, _}
import modelbuilding.solvers.FrehageCompositionalOptimization._

import scala.collection.mutable

/*

# Combining Learning with Compositional Optimiztaion


## Contributions:
  - Can reduce the memory allocation of the learning based when pruning non optimal paths

## Challanges:
  - Direct the learning based on the optimization
    - Identify "target states" and "source states" for the optimization
  - Separate the search on local and shared events
  - DFS on local events in source states of each module
  - BFS on shared events in all combination of local states to determine if they are target states and to identify new source states

TODO:
  [x] Simulation: fix the duration of the actions.
  ## Ideas
  [x] Store the interesting alphabet with each new state. That way the expansion only needs to check those events that CAN add new value.
    i.e interesting evenst are: alphabet\(union of all events in modules that did not have any variable change)
    EDIT: Replace the queue of states with a queue of transitions.
  [ ] Replace for (m <- model.modules) model.modules with "potentially interesting modules", OR CAN WE REALLY?



  */


/**
  * The implementation of the ... as defined in the paper
  * "..."
  * Hagebring et. al. ...
  *
  * @param _sul must be a modular model.
  */
class FrehageCompositionalOptimizationNEW(_sul: SUL) extends BaseSolver {

  val t_start = System.nanoTime()

  val _model = _sul.model
  assert(_model.isModular, "modelbuilder.solver.FrehageCompositionalOptimization requires a modular model.")
  private val model = _model.asInstanceOf[ModularModel]

  private val _sim = _sul.simulator
  assert(_sim.isInstanceOf[TimedCodeSimulator], "modelbuilder.solver.FrehageCompositionalOptimization requires a simulator of type TimedCodeSimulator.")
  private val sim = _sim.asInstanceOf[TimedCodeSimulator]

  private val sharedEvents = model.alphabet.filter(e => model.eventMapping.count(_._2.events.contains(e)) > 1)

  private var sources: List[(StateMap, Alphabet)] = List((_sul.getInitState, model.alphabet))
  private val transitions = collection.mutable.PriorityQueue.empty[(Double,StateMapTransition)](
    Ordering.by((_: (Double, StateMapTransition))._1).reverse
  )

  var moduleTransitions: mutable.Map[String, List[StateMapTransition]] =
    mutable.Map(model.modules.map(_ -> List.empty[StateMapTransition]).toSeq:_*)

  // Keep track of the state variables that should be kept for the final automaton
  private val localVariables: Map[String, mutable.Set[String]] =
    model.modules.map(m => m -> mutable.Set(model.stateMapping(m).states.toArray:_*)).toMap


  def coreach(trans: Seq[(Map[String,Any], Map[String,Any])], targets: Set[Map[String, Any]]): Set[Map[String, Any]] = {
    val next = trans.filter(t => targets.contains(t._2) && !targets.contains(t._1)).map(_._1).toSet
    if (next.isEmpty) targets
    else coreach(trans, targets ++ next)
  }

  // Starting in the initial state, loop until all source states have been expanded
  var maxSize = 0
  var count = 0
  var countTrans = 0

  var t0: Long = 0
  var t1: Long = 0 // time spent before transition loop
  var t2: Long = 0 // time spent updating modules
  var t3: Long = 0 // time spent on sim queries
  var t4: Long = 0
  var t5: Long = 0

  while ({
    count += 1
    sources.nonEmpty // && count < 10
  }) {


    val (source,alphabet) = sources.head

    //    println("")
    //    println(s"### Source: $source.  $alphabet")

    sources = sources.tail
//    var targets: mutable.Set[StateMap] = mutable.Set.empty[StateMap] ++ sim.goalStates.getOrElse(Set.empty[StateMap])
    val sourceTransitions: Map[String, mutable.ListBuffer[StateMapTransition]] =
      model.modules.map(_ -> mutable.ListBuffer.empty[StateMapTransition]).toMap

    // moduleStates: used to distinguish between unique states that might generate new transitions
    val moduleStates: Map[String, mutable.Set[StateMap]] = model.modules
      .map(m => m -> mutable.Set(getReducedStateMap(source, model, m)))
      .toMap

    // localStates: used to identify reduced states that does not require additional transitions
    val localStates: mutable.Map[String, mutable.Set[Map[String, Any]]] = mutable.Map(model.modules.map(m =>
      m ->
        // {if (source != _sul.getInitState)
        mutable.Set(getReducedStateMap(source, model, m).states)
      // else
      //  mutable.Set.empty[Map[String, Any]]}
    ).toSeq:_*)

    // Add the outgoing transitions from the source to the queue to start the expansion
    transitions ++= _sul.getOutgoingTransitions(source, alphabet).map(t => (sim.calculateDuration(t),t))

    // Loop until there are no new paths over local transitions to expand for the current source state.
    while ({
      countTrans += 1
      //      println(s"Trans: $countTrans", s" ${t.event} ${t.source.getState("r_1_l")} ${t.source.getState("r_2_l")} $d ${sim.calculateDuration(t)}")
      transitions.nonEmpty // && count < 10
    })
    {

      val (d, t) = transitions.dequeue()
      //      if (t.event.toString == "e_1_1")
      //        println(s"Trans: $countTrans, ${t.source} ${t.event} ${t.target}")
      //        println(s"Trans: $countTrans, ${
      //          (t.source.getState("r_1_t"), t.source.getState("r_1_l"), t.source.getState("r_1_d"))
      //        } ${
      //          (t.source.getState("r_2_t"), t.source.getState("r_2_l"), t.source.getState("r_2_d"))
      //        } ${
      //          (t.source.getState("r_3_t"), t.source.getState("r_3_l"), t.source.getState("r_3_d"))
      //        }")
//      if (countTrans%1000 == 0)
//        println(s"Trans: $countTrans")
      val isSharedEvent = sharedEvents.events contains t.event


      //      val changedVars = t.target.states.keySet.filter(k => t.source.states(k) != t.target.states(k))
      val changedVars = model.stateMapping.mapValues(s => s.states intersect t.target.states.keySet.filter(k => t.source.states(k) != t.target.states(k)))

      var newStateFound = false
      var possibleCommands: Alphabet = Alphabet()
      for (m <- model.modules) {


//        val changedLocal = model.stateMapping(m).states intersect changedVars
        val tReducedTarget = getReducedStateMap(t.target, model, m)
        val tReducedTargetLocal = tReducedTarget.states.filterKeys(localVariables(m).contains)
        val eventIsPartOfModule = model.eventMapping(m).events contains t.event

        t0 = System.nanoTime() // START OF t1
        /*
         * Only local transitions to queue
         * Shared transitions are added to module and the targets are added to the source queue
         * A local transition is only added if the target has never been seen from the same source(redundant local paths and loops are useless).
         */

        if (eventIsPartOfModule && (isSharedEvent || !localStates(m).contains(tReducedTargetLocal))) {
          sourceTransitions(m) += StateMapTransition(
            getReducedStateMap(t.source, model, m),
            tReducedTarget,
            t.event
          )
        }
        t1 += System.nanoTime()-t0 // END OF t1


        if (changedVars(m).nonEmpty) {
          t0 = System.nanoTime() // START OF t2
          if (!eventIsPartOfModule && (changedVars(m) intersect localVariables(m)).nonEmpty) {
            localVariables(m) --= changedVars(m)
            localStates(m) = localStates(m).map(_.filterKeys(localVariables(m).contains))
          }
          t2 += System.nanoTime() - t0 // END OF t2
          t0 = System.nanoTime()
//          if( !(moduleStates(m) contains tReducedTarget)) {
//            moduleStates(m) += tReducedTarget
//            localStates(m) += tReducedTargetLocal
//            possibleCommands += model.eventMapping(m) intersect alphabet
//            newStateFound = true
//          }
          val x = moduleStates(m).size
          moduleStates(m) += tReducedTarget
          if (x < moduleStates(m).size) {
            localStates(m) += tReducedTargetLocal
            possibleCommands += model.eventMapping(m) intersect alphabet
            newStateFound = true
          }
          t3 += System.nanoTime() - t0
        }
      }

      if (newStateFound) {
        //        println(possibleCommands)
        if (isSharedEvent) {
          sources = (t.target, possibleCommands) :: sources
//          targets += t.source
        }
        else transitions ++= _sul.getOutgoingTransitions(t.target, possibleCommands).map(t => (d + sim.calculateDuration(t),t))
      }

      val size = transitions.size +
        moduleTransitions.map(_._2.size).sum +
        moduleStates.map(_._2.size).sum +
        localStates.map(_._2.size).sum +
        sources.size
      if (size > maxSize) maxSize = size

    }

    /*model.modules.foreach{m =>
      //      println("### " + m)
      //      println(source)
      val trans = sourceTransitions(m).map(t => (t.source.states.filterKeys(localVariables(m).contains), t.target.states.filterKeys(localVariables(m).contains)))
      val t = targets.map(s => s.states.filterKeys(localVariables(m).contains)).toSet
      val cr = coreach(trans, t).toSet
      //      cr foreach println
      //      sourceTransitions(m).foreach(t => println(t, cr.contains(t.target.states.filterKeys(localVariables(m).contains)) || sharedEvents.contains(t.event)))
      moduleTransitions(m) = sourceTransitions(m).filter(t => cr.contains(t.target.states.filterKeys(localVariables(m).contains)) || sharedEvents.contains(t.event)).toList ::: moduleTransitions(m)
    }*/
    model.modules.foreach(m => moduleTransitions(m) = sourceTransitions(m).toList ::: moduleTransitions(m))

  }

  //  states.foreach{ ss =>
  //    println("### " + ss._1)
  //    ss._2 foreach println
  //  }

  val prunedResults: Boolean = true;

  override def getAutomata: Automata = if (prunedResults) getAutomataPruned else getAutomataFull
  def getAutomataPruned: Automata = {
    val modules = for {
      m <- model.modules
      states: Map[String, Map[StateMap, State]] =
        moduleTransitions.map(m => m._1->m._2.flatMap(t => Seq(t.source, t.target)).map(s => {
          val state: StateMap = StateMap(s.states.filterKeys(localVariables(m._1).contains))
          val name = (if (state.equals(_sul.getInitState))
            "INIT: "
          else "") + state.toString
          (s, State(name))
        }).toMap).toMap
      transitions: Set[Transition] =
        moduleTransitions(m)
          .map(t => Transition(
            states(m)(t.source),
            states(m)(t.target),
            t.event // Symbol(EventC(f"(${t.event.toString}, ${sim.calculateDuration(t)}%.2f)"))
          )).toSet[Transition]
      alphabet: Alphabet = Alphabet(transitions.map(_.event))
      iState: State      = states(m)(getReducedStateMap(_sul.getInitState, model, m))
      fState: Option[Set[State]] = _sul.getGoalStates match {
        case Some(gs) => Some(gs.map(s => states(m)(getReducedStateMap(s, model, m))))
        case None     => None
      }
    } yield Automaton(m, states(m).values.toSet, alphabet, transitions, iState, fState)
    core.Automata(modules)
  }

  def getAutomataFull: Automata = {
    val modules = for {
      m <- model.modules
      states: Map[String, Map[StateMap, State]] =
        moduleTransitions.map(m => m._1->m._2.flatMap(t => Seq(t.source, t.target)).map(s => {
          val state: StateMap = s
          val name = (if (state.equals(_sul.getInitState))
            "INIT: "
          else "") + state.toString
          (s, State(name))
        }).toMap).toMap
      transitions: Set[Transition] = moduleTransitions(m)
        .map(t => Transition(
          states(m)(t.source),
          states(m)(t.target),
          t.event // Symbol(EventC(f"(${t.event.toString}, ${sim.calculateDuration(t)}%.2f)"))
        )).toSet[Transition]
      alphabet: Alphabet = Alphabet(transitions.map(_.event))
      iState: State      = states(m)(getReducedStateMap(_sul.getInitState, model, m))
      fState: Option[Set[State]] = _sul.getGoalStates match {
        case Some(gs) => Some(gs.map(s => states(m)(getReducedStateMap(s, model, m))))
        case None     => None
      }
    } yield core.Automaton(
      m,
      states(m).values.toSet,
      alphabet,
      transitions,
      iState,
      fState
    )
    core.Automata(modules)
  }

  val time_total = System.nanoTime()-t_start

}
