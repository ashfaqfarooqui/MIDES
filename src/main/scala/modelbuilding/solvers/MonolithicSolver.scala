package modelbuilding.solvers

import modelbuilding.core.modeling.Model
import modelbuilding.core.{
  Automata,
  Automaton,
  SUL,
  State,
  StateMap,
  StateMapTransition,
  Symbol,
  Transition
}

import scala.collection.immutable.Queue

class MonolithicSolver(sul: SUL) extends BaseSolver {

  //val simulation = sul.simulation
  val initState = sul.getInitState
  val model     = sul.model

  val events: Set[Symbol]    = model.alphabet.events
  val visitedStates          = Set.empty[StateMap]
  val queue: Queue[StateMap] = Queue(initState)

  def explore(
      queue: Queue[StateMap],
      visitedSet: Set[StateMap],
      arcs: Set[StateMapTransition]
    ): Set[StateMapTransition] = {
    if (queue.isEmpty) {
      arcs
    } else {
      var transitions: Set[StateMapTransition] = arcs
      val currState                            = queue.dequeue
      val visited                              = visitedSet + currState._1

      info(s"current queue: $queue")
      val reachedStates = events.map(
        e =>
          sul.getNextState(currState._1, e.getCommand) match {
            case Some(value) =>
              transitions = transitions + StateMapTransition(currState._1, value, e)
              info(s"transition found: $transitions")
              Some(value)
            case _ =>
              None
          }
      )

      val updq = reachedStates
        .filter(_.isDefined)
        .filterNot(a => visited.contains(a.get))
        .foldLeft(currState._2) { (q, v) =>
          q :+ v.get
        }
      explore(updq, visited, transitions)
    }

  }

  def getStatesFromTransitions(t: Set[StateMapTransition]): Set[StateMap] = {
    t.foldLeft(Set(initState)) { (s, t) =>
      s + t.target
    }
  }

  def mapStates(states: Set[StateMap]): Map[StateMap, State] = {
    states.zipWithIndex.toMap.map {
      case (sm, i) => (sm, if (sm.equals(initState)) State("init") else State(s"s$i"))
    }
  }

  def mapTransitions(trans: Set[StateMapTransition]): Set[Transition] = {
    trans.map { t =>
      Transition(mappedStates(t.source), mappedStates(t.target), t.event)
    }
  }

  //To convert into the format we already have
  /* def createTransitionTable(stateMapping:Map[StateMap,State],transitions:Set[Transition]): (State,Symbol)=>State={

    val tranFunc = transitions.map{
      t=>
        (stateMapping(t.tail), t.event) -> stateMapping(t.head)
    }.toMap

    def f(s:State,e:Symbol):State={
      if(tranFunc.isDefinedAt((s,e))){
      tranFunc((s,e))}
      else State("dump:")
    }
    f
  }*/

  info("Starting to build the models using MonolithicSolver")

  val transitions       = explore(queue, visitedStates, Set.empty[StateMapTransition])
  val mappedStates      = mapStates(getStatesFromTransitions(transitions))
  val mappedTransitions = mapTransitions(transitions)

  info(s"Mapped states: $mappedStates")
  // val aut= Automaton(mappedStates.values.toSet + State("dump:"),createTransitionTable(mappedStates,transitions),model.alphabet,mappedStates(initState),None,None)

  override def getAutomata: Automata = {
    Automata(
      Set(
        Automaton(
          model.name,
          mappedStates.values.toSet,
          model.alphabet,
          mappedTransitions,
          mappedStates(initState),
          None,
          None
        )
      )
    )
  }

}
