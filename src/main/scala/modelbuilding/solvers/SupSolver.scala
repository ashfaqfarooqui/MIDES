package modelbuilding.solvers
import SupremicaStuff.SupremicaHelpers
import grizzled.slf4j.Logging
import modelbuilding.core.{Alphabet, Automata, Automaton, State, StateMap, StateMapTransition, Symbol, Transition, Uncontrollable}
import modelbuilding.core.modelInterfaces.{Model, ModularModel, MonolithicModel}
import modelbuilding.solvers.SupSolver._
import net.sourceforge.waters.subject.module.ModuleSubject
import org.supremica._

import scala.collection.JavaConverters._
import scala.collection.immutable.Queue


object SupSolver {
  def aggregateModulesForModel(m:ModularModel,sp:Set[automata.Automaton]) = {
    sp.map {
      s =>
        val specAlphabet = s.getAlphabet
        //This should work for the base case -- we get a supervisor, but not a supremal controllable supervisor
        def selectedAlphabet = m.eventMapping filter {
          case (k: String, v: Set[Symbol]) => v.exists(x => specAlphabet.contains(x.toString))
        }

        val selAlphabet = Alphabet(selectedAlphabet.values.toSet.foldLeft(Set.empty[Symbol]) {
          (acc, i) => acc union i.events
        } union m.alphabet.a.filter(a => specAlphabet.contains(a.toString)))
        s -> selAlphabet
    }.toMap
  }

  def extendStateMap(sp:Set[automata.Automaton],st:StateMap): StateMap ={
    StateMap(state=st.state ++  sp.map(s=> s.getName-> s.getInitialState.getName).toMap)
  }


  }

class SupSolver(_model:Model) extends BaseSolver with SupremicaHelpers with Logging{

  assert(_model.specFilePath.isDefined, "modelbuilder.solver.SupSolver requires a specification model.")
  info("Initializing SupSolver")
  override val mModule: ModuleSubject = ReadFromWmodFile(_model.specFilePath.get).get
  val specs = getSupremicaAutomataFromWaters(mModule).get.filter(_.isSpecification).toSet
  //lets assume single spec for simplicity
  val spec = specs.head

  info(s"Read Specifications ${spec.getName}")

  val model = if (_model.isModular) {
    _model.asInstanceOf[ModularModel]
  } else _model.asInstanceOf[MonolithicModel]


  val events:Set[Symbol] = model.alphabet.events
  val sul = model.simulation
  val initState = extendStateMap(specs, sul.getInitState)


  //experimenting with monolithin first


  val visitedStates = Set.empty[StateMap]
  val queue: Queue[StateMap] = Queue(initState)

  def getNextSpecState(sp:automata.Automaton,st:StateMap, c:Symbol):Option[StateMap]={
    val specStateInMap=st.state(sp.getName).asInstanceOf[String]
    info(s"StateMap state of Spec $specStateInMap Symbol ${c.toString}")
    val currSpecState = sp.getStateSet.getState(st.state(sp.getName).asInstanceOf[String])
    info(s"current state transitions: ${currSpecState.getOutgoingArcs.asScala.filter(_.getSource.getName==specStateInMap).filter(_.getEvent.getName==c.toString)},")
    //since we know the spec is deterministic there can exist just one or none transitions
    val theTransition = currSpecState.getOutgoingArcs.asScala.filter(_.getSource.getName==specStateInMap).filter(_.getEvent.getName==c.toString)
   info(s"found Transition $theTransition")
    if(theTransition.isEmpty)
      None
    else{
      Some(st.next(sp.getName,theTransition.head.getTarget.getName))
    }
  }

  def explore(queue: Queue[StateMap], visitedSet: Set[StateMap], arcs: Set[StateMapTransition]): Set[StateMapTransition] = {
    if (queue.isEmpty) {
      arcs
    }

    else {
      var transitions: Set[StateMapTransition] = arcs
      val currState = queue.dequeue
      val visited = visitedSet + currState._1

      info(s"current queue: $queue")
      val reachedStates = events.map(e =>
        sul.getNextState(currState._1, e.getCommand) match {
          case Some(value) => getNextSpecState(spec, value, e) match {
            case Some(v) =>
              transitions = transitions + StateMapTransition(currState._1, v, e)
              info(s"transition found: $transitions")
              Some(v)
            case _ => None
          }
          case _ =>
            None
        })

      val updq = reachedStates.filter(_.isDefined).filterNot(a => visited.contains(a.get)).foldLeft(currState._2) {
        (q, v) => q :+ v.get
      }
      explore(updq, visited, transitions)
    }


  }


  def getStatesFromTransitions(t:Set[StateMapTransition]):Set[StateMap]={
    t.foldLeft(Set(initState)){
      (s,t)=>  s + t.target
    }
  }

  def mapStates(states:Set[StateMap]):Map[StateMap,State]={
    states.zipWithIndex.toMap.map{case(sm,i)=> (sm,if(sm.equals(initState)) State("init") else State(s"s$i"))}
  }

  def mapTransitions(trans:Set[StateMapTransition]): Set[Transition] ={
    trans.map{
      t=> Transition(mappedStates(t.source),mappedStates(t.target),t.event)
    }
  }

  info("Starting to build the models using MonolithicSolver")

  val transitions = explore(queue,visitedStates,Set.empty[StateMapTransition])
  val mappedStates= mapStates(getStatesFromTransitions(transitions))
  val mappedTransitions = mapTransitions(transitions)

  info(s"Mapped states: $mappedStates")
  // val aut= Automaton(mappedStates.values.toSet + State("dump:"),createTransitionTable(mappedStates,transitions),model.alphabet,mappedStates(initState),None,None)


  override def getAutomata: Automata = {
    Automata(Set(Automaton(model.name,mappedStates.values.toSet,model.alphabet,mappedTransitions,mappedStates(initState),None,None)))
  }

}