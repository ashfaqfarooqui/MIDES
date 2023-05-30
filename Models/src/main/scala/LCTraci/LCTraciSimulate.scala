package LCTraci

import modelbuilding.core._//{Action, Command, Predicate, StateMap, CompoundPredicate}
import modelbuilding.externalClients.TraCI.TraCISimulator

class LCTraciSimulate extends TraCISimulator{

  val INITIATION_STEP = 10
  val ALLOWED_FAULTS = 1
  val DELTA_TIME = 1 // We should divide this by 10 when sending it to SUMO. The time line of an experiment will then be from 0-420 indicating 0-42s



  val time = "time"
  val NrFaults = "NrFaults"
  val error = "error"
  val crash = "crash"
  val lca = "LCA"
  override val initState: StateMap = StateMap(time->0,NrFaults->0,crash->0,error->false,lca->1)
  override val goalStates: Option[Set[StateMap]] = Some(Set(StateMap(time->42,error->false,crash->false)))
  override val guards: Map[Command, Predicate] = Map(
    faultyStep-> AND(GREQ(time,INITIATION_STEP),LEQ(NrFaults,ALLOWED_FAULTS))
  )
  override val actions: Map[Command, List[Action]] = Map(
    normalStep -> List(Incr(time,DELTA_TIME)),
    faultyStep-> List(Incr(time,DELTA_TIME),Incr(NrFaults,1))
  )
}

object LCTraciSimulate {
  def apply(): LCTraciSimulate = new LCTraciSimulate()
}