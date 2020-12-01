package MachineBuffer

import modelbuilding.core._
trait MachineBufferDomain {
  override def toString: String = this match {
    case `load1`   => "load1"
    case `load2`   => "load2"
    case `unload1` => "unload1"
    case `unload2` => "unload2"

  }
}

case object load1   extends Command with MachineBufferDomain with Controllable
case object load2   extends Command with MachineBufferDomain with Controllable
case object unload1 extends Command with MachineBufferDomain with Uncontrollable
case object unload2 extends Command with MachineBufferDomain with Uncontrollable
