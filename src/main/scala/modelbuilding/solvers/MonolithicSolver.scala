package modelbuilding.solvers

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
import scala.annotation.tailrec

class MonolithicSolver(sul: SUL) extends BaseSolver {

  //val simulation = sul.simulation
  val initState = getReducedState(sul.getInitState)
  val model     = sul.model

  val events: Set[Symbol]    = model.alphabet.events
  val visitedStates          = Set.empty[StateMap]
  val queue: Queue[StateMap] = Queue(initState)

  def getReducedState(sp: StateMap): StateMap = {
    StateMap(sp.name, states = sp.states.filterKeys(model.states.states.contains))
  }
  def extend(s: StateMap): StateMap = {
    //THis function is only to test lane change....
    val laneChngReq = "laneChngReq"
    val b3          = "b3"
    val b4          = "b4"
    val b5          = "b5"
    val b6          = "b6"
    val b7          = "b7"
    val b8          = "b8"
    val b9          = "b9"
    val b10         = "b10"
    val b11         = "b11"
    val b12         = "b12"

    val initialDecisionMap =
      Map(
        // b1  -> false,
        // b2  -> false,
        b3  -> false,
        b4  -> false,
        b5  -> false,
        b6  -> false,
        b7  -> false,
        b8  -> false,
        b9  -> false,
        b10 -> false,
        b11 -> false,
        b12 -> false
      )

    StateMap(states = s.states + (laneChngReq -> "none") ++ initialDecisionMap)
  }

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

      info(s"current queue size: ${queue.size}")
      val reachedStates = events.map(
        e =>
          sul.getNextState(extend(currState._1), e.getCommand) match {
            case Some(value) =>
              transitions = transitions + StateMapTransition(
                currState._1,
                getReducedState(value),
                e
              )
              info(s"transition found: $transitions")
              Some(getReducedState(value))
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
      case (sm, i) =>
        (
          sm,
          if (sm.equals(initState)) State(s"init ${sm.states}")
          else State(s"${sm.states}")
        )
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
  // val aut= Automaton(mappedStates.values.toSet + State("dump:"),createTransitionTable(mappedStates,transitions), model.alphabet,mappedStates(initState),None,None)

  val ptm = ParseToModule(model.name, transitions)
  ptm.saveToWMODFile(s"./Output/${model.name}.wmod")

  import supremicastuff.FlowerPopulater
  import supremicastuff._
  ////Testing\

  case class ParseToModule(
      moduleName: String,
      // vars: Set[String],
      trans: Set[StateMapTransition])
      extends FlowerPopulater
      with Exporters {

    override val mModule = SimpleModuleFactory(moduleName)
    /*
    val varDomainMap = Map(
      "direction" -> List("none", "right", "left"),
      "state" -> List(
        "stateA",
        "stateB",
        "stateC",
        "stateD",
        "stateE",
        "stateF",
        "stateG"
      ),
      "b2"                -> List("false", "true"),
      "b1"                -> List("false", "true"),
      "laneChangeRequest" -> List("false", "true")
    )
    varDomainMap.foreach {
      case (k, v) =>
        println(s"adding variable $k with domain $v")
        addVariable(k, v.toSet, v.head, Set(v.head))
    }*/

    //addFlowerRoot(s"hyp_$moduleName")

    addVariablesWithDomain(initState,trans)
    val efa = addFlowerRoot(s"hyp_$moduleName")
    trans.foreach { t =>
      addEventIfNeededElseReturnExistingEvent(t.event.toString(), false)
      addLeafToEfa(
        efa.get,
        t.event.toString,
        stateMapToSupremicaSyntax(t.source),
        transitionToActions(t).mkString(";")
      )

    }

    /*    val evtTransMap = events.map { e =>
      e -> trans.filter(t => t.event == e)
    }.filter{
      case (e,t)=> t.nonEmpty
    }

    evtTransMap.map {
      case (e, t) =>
        addLeafAndEventToAlphabet(e.toString(), false, createGuards(t), createActions(t))

    }*/

    def addVariablesWithDomain(init: StateMap, trans: Set[StateMapTransition]) = {
//We assume the init state == marked state. Needs to be updated

      val variables: Set[String] = init.states.keySet
      val variableInitMap: Map[String, String] =
        init.states.map(x => x._1 -> x._2.toString())

      @tailrec
      def populateDomain(
          remainingTransitions: List[StateMapTransition],
          dom: Map[String, Set[String]]
        ): Map[String, Set[String]] = {

        remainingTransitions match {
          case x :: xs =>
            val agg: Map[String, Set[String]] = variables.map { v =>
              val aggDomain = dom.getOrElse(v,Set.empty[String]) + x.target.states(v).toString()
              (v -> aggDomain)
            }.toMap
            populateDomain(xs, dom ++ agg)
          case Nil => dom
        }
      }
      val doms = populateDomain(trans.toList, Map.empty[String, Set[String]])
      variables.foreach(
        k => addVariable(k, doms(k).toSet, variableInitMap(k), Set(variableInitMap(k)))
      )

    }
    def createGuards(tr: Set[StateMapTransition]) = {
      tr.map(t => stateMapToSupremicaSyntax(t.source)).mkString(" | ")
    }

    def createActions(tr: Set[StateMapTransition]) = {

      val actions = tr.map(transitionToActions(_))

      actions.flatten.mkString(";")
    }

    private def stateMapToSupremicaSyntax(s: StateMap): String = {

      val g = s.states.map { x =>
        s"${x._1}==${x._2}"
      }

      g.mkString(" & ")

    }

    private def transitionToActions(t: StateMapTransition) = {

      val actions = t.source.states.keys
        .filter(k => t.source.states(k) != t.target.states(k))
        .map(k => s"$k=${t.target.states(k)}")
      println(actions.mkString("; "))
      actions

    }

  }

  ////
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
