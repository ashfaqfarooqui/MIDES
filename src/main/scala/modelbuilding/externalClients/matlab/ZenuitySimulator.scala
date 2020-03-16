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

package modelbuilding.externalClients.matlab
import grizzled.slf4j.Logging
import modelbuilding.core._
import com.mathworks.engine.MatlabEngine
import com.mathworks.matlab.types.Struct
import modelbuilding.core.interfaces.simulator.{Simulator, TwoStateOperation}
import modelbuilding.helpers.ConfigHelper

import scala.collection.JavaConverters._
import scala.concurrent.Future
trait ZenuitySimulator extends Simulator with TwoStateOperation with Logging {

  private var matlabClient: Option[MatlabClient] = None

  def getClient = {
    if (matlabClient.isEmpty) matlabClient = Some(MatlabClient())
    matlabClient.get
  }

  //TODO: put back reset
  def getInitialState: StateMap = {
    getClient.loadPath(ConfigHelper.matlabPath)
    // getClient.reset
    getClient.getState
  } //.

  override def evalCommandToRun(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Option[Boolean] = {
    c match {
      case `reset`                => Some(true)
      case `tau`                  => Some(true)
      case x if guards contains x => guards(x).eval(s, acceptPartialStates)
      case y                      => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  override def translateCommand(c: Command): List[Action] = {
    c match {
      case `reset`                 => List(ResetAction)
      case `tau`                   => List(TauAction)
      case x if actions contains x => actions(x)
      case y                       => throw new IllegalArgumentException(s"Unknown command: `$y`")
    }
  }

  val state             = "state"
  val direction         = "direction"
  val laneChangeRequest = "laneChangeRequest"
  val laneChngReq       = "laneChngReq"
  val b1                = "b1"
  val b2                = "b2"
  val b3                = "b3"
  val b4                = "b4"
  val b5                = "b5"
  val b6                = "b6"
  val b7                = "b7"
  val b8                = "b8"
  val b9                = "b9"
  val b10               = "b10"
  val b11               = "b11"
  val b12               = "b12"

  def arrayToStateMap(r: java.lang.Object): StateMap = {
    val response = r.asInstanceOf[Array[java.lang.Object]]
    val sMap = Map(
      state             -> response(0).asInstanceOf[String],
      direction         -> response(1).asInstanceOf[String],
      laneChangeRequest -> response(2).asInstanceOf[Boolean],
      b1                -> response(3).asInstanceOf[Boolean],
      b2                -> response(4).asInstanceOf[Boolean],
      laneChngReq       -> response(5).asInstanceOf[String],
      b3                -> response(6).asInstanceOf[Boolean],
      b4                -> response(7).asInstanceOf[Boolean],
      b5                -> response(8).asInstanceOf[Boolean],
      b6                -> response(9).asInstanceOf[Boolean],
      b7                -> response(10).asInstanceOf[Boolean],
      b8                -> response(11).asInstanceOf[Boolean],
      b9                -> response(12).asInstanceOf[Boolean],
      b10               -> response(13).asInstanceOf[Boolean],
      b11               -> response(14).asInstanceOf[Boolean],
      b12               -> response(15).asInstanceOf[Boolean]
    )

    StateMap(state = sMap)
  }

  def stateMapToArray(sMap: StateMap): Array[java.lang.Object] = {

    val value_state             = sMap.getKey(state).get
    val value_direction         = sMap.getKey(direction).get
    val value_laneChangeRequest = sMap.getKey(laneChangeRequest).get
    val value_b1                = sMap.getKey(b1).get
    val value_b2                = sMap.getKey(b2).get
    val value_laneChngReq       = sMap.getKey(laneChngReq).get
    val value_b3                = sMap.getKey(b3).get
    val value_b4                = sMap.getKey(b4).get
    val value_b5                = sMap.getKey(b5).get
    val value_b6                = sMap.getKey(b6).get
    val value_b7                = sMap.getKey(b7).get
    val value_b8                = sMap.getKey(b8).get
    val value_b9                = sMap.getKey(b9).get
    val value_b10               = sMap.getKey(b10).get
    val value_b11               = sMap.getKey(b11).get
    val value_b12               = sMap.getKey(b12).get

    Array(
      value_state.asInstanceOf[String],
      value_direction.asInstanceOf[String],
      Boolean.box(value_laneChangeRequest.asInstanceOf[Boolean]),
      Boolean.box(value_b1.asInstanceOf[Boolean]),
      Boolean.box(value_b2.asInstanceOf[Boolean]),
      value_laneChngReq.toString.asInstanceOf[String],
      Boolean.box(value_b2.asInstanceOf[Boolean]),
      Boolean.box(value_b3.asInstanceOf[Boolean]),
      Boolean.box(value_b4.asInstanceOf[Boolean]),
      Boolean.box(value_b5.asInstanceOf[Boolean]),
      Boolean.box(value_b6.asInstanceOf[Boolean]),
      Boolean.box(value_b7.asInstanceOf[Boolean]),
      Boolean.box(value_b8.asInstanceOf[Boolean]),
      Boolean.box(value_b9.asInstanceOf[Boolean]),
      Boolean.box(value_b10.asInstanceOf[Boolean]),
      Boolean.box(value_b11.asInstanceOf[Boolean]),
      Boolean.box(value_b12.asInstanceOf[Boolean])
    )
  }

  def stateMapToStruct(sMap: StateMap): Struct = {
    val value_state             = sMap.getKey(state).get
    val value_direction         = sMap.getKey(direction).get
    val value_laneChangeRequest = sMap.getKey(laneChangeRequest).get
    val value_b1                = sMap.getKey(b1).get
    val value_b2                = sMap.getKey(b2).get
    val value_laneChngReq       = sMap.getKey(laneChngReq).get
    val value_b3                = sMap.getKey(b3).get
    val value_b4                = sMap.getKey(b4).get
    val value_b5                = sMap.getKey(b5).get
    val value_b6                = sMap.getKey(b6).get
    val value_b7                = sMap.getKey(b7).get
    val value_b8                = sMap.getKey(b8).get
    val value_b9                = sMap.getKey(b9).get
    val value_b10               = sMap.getKey(b10).get
    val value_b11               = sMap.getKey(b11).get
    val value_b12               = sMap.getKey(b12).get

    new Struct(
      state,
      value_state.asInstanceOf[String],
      direction,
      value_direction.asInstanceOf[String],
      laneChangeRequest,
      Boolean.box(value_laneChangeRequest.asInstanceOf[Boolean]),
      b1,
      Boolean.box(value_b1.asInstanceOf[Boolean]),
      b2,
      Boolean.box(value_b2.asInstanceOf[Boolean]),
      laneChngReq,
      value_laneChngReq.toString.asInstanceOf[String],
      b3,
      Boolean.box(value_b3.asInstanceOf[Boolean]),
      b4,
      Boolean.box(value_b4.asInstanceOf[Boolean]),
      b5,
      Boolean.box(value_b5.asInstanceOf[Boolean]),
      b6,
      Boolean.box(value_b6.asInstanceOf[Boolean]),
      b7,
      Boolean.box(value_b7.asInstanceOf[Boolean]),
      b8,
      Boolean.box(value_b8.asInstanceOf[Boolean]),
      b9,
      Boolean.box(value_b9.asInstanceOf[Boolean]),
      b10,
      Boolean.box(value_b10.asInstanceOf[Boolean]),
      b11,
      Boolean.box(value_b11.asInstanceOf[Boolean]),
      b12,
      Boolean.box(value_b12.asInstanceOf[Boolean])
    )
  }

  override def runCommand(
      c: Command,
      s: StateMap,
      acceptPartialStates: Boolean
    ): Either[StateMap, StateMap] = {

    evalCommandToRun(c, s, acceptPartialStates) match {
      case Some(true) =>
        debug(s"running command $c, on state $s")

        c match {
          case `reset` =>
            Right(s)
          case `tau` => Right(s)
          case _ =>
            val currState = translateCommand(c).foldLeft(s) { (acc, ac) =>
              ac.next(acc)
            }
            val resp = Right(
              arrayToStateMap(
                getClient.runFunction(
                  16,
                  ConfigHelper.matlabProgram,
                  stateMapToStruct(currState)
                )
              )
            )

            debug(s"get resp $resp")
            resp

        }
      case Some(false) => Left(s)
      case None =>
        throw new IllegalArgumentException(
          s"Can not evaluate Command `$c`, since Partial state `$s` does not include all affected variables."
        )
    }
  }

  override def runListOfCommands(
      commands: List[Command],
      s: StateMap
    ): Either[StateMap, StateMap] = {
    @scala.annotation.tailrec
    def runList(c: List[Command], ns: StateMap): Either[StateMap, StateMap] = {

      c match {
        case x :: xs =>
          //println(s"running element $x of the list $c ")

          runCommand(x, ns) match {
            case Right(n) => runList(xs, n)
            case Left(n)  => Left(n)
          }
        case Nil => Right(ns)
      }

    }
    //println(s"running list $commands")
    runList(commands, s)

  }
}
