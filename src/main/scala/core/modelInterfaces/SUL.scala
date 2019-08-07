package core.modelInterfaces

import core.{Command, StateMap}

abstract class SUL {

  val simulator:Simulator
  def getNextState(currState:StateMap,a:Command):Either[StateMap,StateMap]={

    simulator.runCommand(a,currState)
  }
}
