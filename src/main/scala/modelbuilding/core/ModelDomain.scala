package modelbuilding.core

trait Controllable
trait Uncontrollable

trait Command {
  override def toString: String = this match {
    case `reset` => "reset"
    case `tau`   => "tau"

  }

}
class ControllableCommand(name: String) extends Command with Controllable {
  override def toString: String = name
}
class UncontrollableCommand(name: String) extends Command with Uncontrollable {
  override def toString: String = name
}

case object reset extends Command with Controllable
case object tau   extends Command with Controllable
