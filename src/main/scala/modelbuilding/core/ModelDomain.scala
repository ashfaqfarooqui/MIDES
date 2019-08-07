package modelbuilding.core

trait Controllable
trait Uncontrollable

trait Command {
  override def toString: String = this match {
    case `reset` => "reset"
    case `tou` => "tou"
  }


}

case object reset extends Command with Controllable
case object tou extends Command with Controllable
