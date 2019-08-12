package modelbuilding.solvers
import SupremicaStuff.SupremicaHelpers
import grizzled.slf4j.Logging
import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.core.{Alphabet, Automata, Automaton, State, StateMap, StateMapTransition, Symbol, Transition, Uncontrollable}
import modelbuilding.core.modelInterfaces.{Model, ModularModel, MonolithicModel}
import modelbuilding.solvers.ModularSupSolver._
import modelbuilding.solvers.MonolithicSupSolver.extendStateMap
import net.sourceforge.waters.subject.module.ModuleSubject
import org.supremica.automata

import scala.collection.immutable.Queue
import scala.collection.JavaConverters._

object ModularSupSolver{
  def aggregateModulesForModel(m:ModularModel,sp:Set[automata.Automaton]) = {
    sp.map {
      s =>
        val specAlphabet = s.getAlphabet
        //This should work for the base case -- we get a supervisor, but not a supremal controllable supervisor
        val selectedAlphabet = m.eventMapping filter {
          case (k: Module, v: Alphabet) => v.events.exists(x => x.getCommand.isInstanceOf[Uncontrollable] && specAlphabet.contains(x.toString))
        }
        println(s"Selected alphabet: $selectedAlphabet")
        val selAlphabet = selectedAlphabet.values.toSet.foldLeft(Set.empty[Symbol]) {
          (acc, i) => acc union i.events
        } union m.alphabet.events.filter(a => specAlphabet.contains(a.toString))

        //Dirty hack, Alphabet does not take a set as is.
        s -> new Alphabet(selAlphabet.asInstanceOf[Set[Any]])
    }.toMap
  }

  //Remove spec variable
  def getReducedState(sp:StateMap,module: Module,allModules:Set[Module]):StateMap= {
    sp.removeKeys(allModules.filterNot(_ == module))
  }
}

class ModularSupSolver(_model:Model) extends BaseSolver with SupremicaHelpers with  Logging {

  assert(_model.specFilePath.isDefined, "modelbuilder.solver.ModularSupSolver requires a specification.")
  assert(_model.isModular, "modelbuilder.solver.ModularSupSolver requires a modular model.")

  info("Initializing ModularSupSolver")
  override val mModule: ModuleSubject = ReadFromWmodFile(_model.specFilePath.get).get
  val specs = getSupremicaAutomataFromWaters(mModule).get.filter(_.isSpecification).toSet



  specs.foreach(s=>info(s"Read spec for ${s.getName}"))

  val model = _model.asInstanceOf[ModularModel]
  val sul = model.simulation
  val initState = extendStateMap(specs, sul.getInitState)


  val initQueue:Queue[StateMap] = Queue(initState)
  val moduleMapping = aggregateModulesForModel(model,specs)
  println(s"module mapping $moduleMapping")

  def initMaps[T] = moduleMapping.keySet.map((_.getName.asInstanceOf[Module]->Set.empty[T])).toMap


  private var moduleTransitions: Map[Module, Set[StateMapTransition]] = moduleMapping.map(_._1.getName -> Set.empty[StateMapTransition]).toMap
  private var forbiddenStates:Map[Module,Set[StateMap]]=initMaps[StateMap]

  def getNextSpecState(sp:automata.Automaton,st:StateMap, c:Symbol):Option[StateMap]={
    if(!sp.getAlphabet.contains(c.toString)){
      return Some(st)
    }
    val specStateInMap=st.state(sp.getName).asInstanceOf[String]
    val currSpecState = sp.getStateSet.getState(specStateInMap)
    //since we know the spec is deterministic there can exist just one or none transitions
    val theTransition = currSpecState.getOutgoingArcs.asScala.filter(_.getSource.getName==specStateInMap).filter(_.getEvent.getName==c.toString)
    if(theTransition.isEmpty)
      None
    else{
      Some(st.next(sp.getName,theTransition.head.getTarget.getName))
    }
  }


  def explore(queue:Queue[StateMap], moduleStates:Map[Module,Set[StateMap]],visitedSet:Map[Module,Set[StateMap]], arcs:Map[Module,Set[StateMapTransition]]):(Map[Module,Set[StateMap]],Map[Module,Set[StateMapTransition]])={
    info(s"Starting to explore with queue size ${queue.size}")
    if(queue.isEmpty){
      (moduleStates,arcs)
    }
    else{
      var transitions: Map[Module,Set[StateMapTransition]] = arcs
      var visited:Map[Module,Set[StateMap]] =visitedSet
      var mStates:Map[Module,Set[StateMap]] = moduleStates
      val currState = queue.dequeue
      var newQueue = currState._2
      for(m<-moduleMapping.keySet){
        val module = m.getName
        info(s"working on module $module")


        visited= visited + (module->(visitedSet(module) + currState._1))
        info(s"modulestates $moduleStates")

        if(moduleStates(module).contains(currState._1)) {
//only iterate over reachable states

          def reachedStates = moduleMapping(m).events.map { e =>
            sul.getNextState(currState._1, e.getCommand) match {
              case Some(value) => getNextSpecState(m, value, e) match {
                case Some(v) => if (!forbiddenStates(module).contains(currState._1)) {
                  transitions += (module -> (transitions(module) + StateMapTransition(currState._1, v, e)))
                  Some(v)
                } else {
                  None
                }
                case _ =>
                  if (e.getCommand.isInstanceOf[Uncontrollable]) {
                    forbiddenStates += (module -> (forbiddenStates(module) + currState._1))
                  }
                  None
              }
              case _ =>
                None
            }
          }.filter(_.isDefined).map(_.get)

          info(s"computed reached states of size: ${reachedStates.size}")
          mStates = mStates + (module -> (mStates(module) ++ reachedStates))

          newQueue = reachedStates.diff(visited(module)).filterNot(a => newQueue.contains(a)).foldLeft(newQueue) {
            (q, v) => q :+ v
          }
        }
      }


      explore(newQueue,mStates,visited,transitions)

    }
  }


  def initModuleState = moduleMapping.keySet.map(_.getName.asInstanceOf[Module]-> Set(initState)) toMap

  val (mStates,mTransitions) = explore(initQueue,initModuleState,initMaps[StateMap],initMaps[StateMapTransition])

  for(m<-moduleMapping.keySet) {
    info(s"nbr of states  ${mStates(m.getName).size}")
    info(s"nbr of Trans  ${mTransitions(m.getName).size}")
  }
  info(s"found states $mStates")



//we shouldnt have to do this
  def getStatesFromTransitions(t:Set[StateMapTransition]):Set[StateMap]={
    t.foldLeft(Set(initState)){
      (s,t)=>  s + t.target
    }
  }


  def mapStates(states:Set[StateMap]):Map[StateMap,State]={
    states.zipWithIndex.toMap.map{case(sm,i)=> (sm,if(sm.equals(initState)) State("init") else State(s"s$i"))}
  }

  def mapTransitions(stateMapping:Map[StateMap,State],trans:Set[StateMapTransition]): Set[Transition] ={
    trans.map{
      t=> Transition(stateMapping(t.source),stateMapping(t.target),t.event)
    }
  }




  override def getAutomata: Automata = {
    val supAut=new automata.Automata()

    val modules = moduleMapping.keySet.map{m=> val module=m.getName
      val mappedStates = mapStates(mStates(module) union forbiddenStates(module) union getStatesFromTransitions(mTransitions(module)))
      val mappedTransitions= mapTransitions(mappedStates,mTransitions(module))
      val fbdStates= if(forbiddenStates(module).nonEmpty) Some(forbiddenStates(module) map mappedStates) else None



      supAut.addAutomaton(createSupremicaAutomaton(mappedStates.values.toSet,
        mappedTransitions,
        moduleMapping(m),
        mappedStates(initState),
        None,
        fbdStates,
        module))


      Automaton(module,
        mappedStates.values.toSet,
        moduleMapping(m),
        mappedTransitions,
        mappedStates(initState),
        None,
        fbdStates)
    }

    saveToXMLFile(s"Output/result_${model.name}.xml",supAut)
    Automata(modules)
  }



}
