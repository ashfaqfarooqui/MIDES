package modelbuilding.core

trait Controllable
trait Uncontrollable

trait Command {
  override def toString: String = this match {
    case `reset` => "reset"
    case `tau`   => "tau"

  }

}

case object reset extends Command with Controllable
case object tau   extends Command with Controllable
