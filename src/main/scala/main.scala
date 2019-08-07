package ModelBuilding

import core._
import main.scala.models.CatAndMouse._


import main.scala.models.RobotArm._
import main.scala.models.StickPicking._
import models.MachineBuffer._
import grizzled.slf4j.Logging
import models.TestUnit.TransferLine

import scala.collection.JavaConverters._



object ModelBuilder extends Logging {

    val sim = new SULMachineBuffer
  case class Transition(head:StateMap,tail:StateMap,event:Command)
  val learningType = "mono" //"modular"

    def main(args: Array[String]) :Unit= {

      info("Automata learn!")


      if(learningType=="modular"){

      }
      if(learningType=="mono"){



      }

    }

  def automatonBuilder(stateList:Set[(Int,StateMap)],trans:Set[Transition],init:StateMap,mState:Set[StateMap])={


  }
}
