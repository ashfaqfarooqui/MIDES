package modelbuilding.core

sealed trait Predicate {

  private implicit class inequalityAttributes(l: Any) {
    def >(r: Any) = {
      (l, r) match {
        case (l: Int, r: Int)     => l > r
        case (l: Float, r: Float) => l > r
        case _                    => false
      }
    }

    def <(r: Any) = r > l

    def >=(r: Any) = (l > r) || (l == r)

    def <=(r: Any) = (r > l) || (l == r)

  }

  def eval(s: StateMap, allowPartialStates: Boolean = false): Option[Boolean] = {
    def evalEqualities(l: String, r: Any, e: (Any, Any) => Boolean) = {
      s.get(l) match {
        case Right(v) => Some(e(v, r))
        case Left(_)  => None
      }
    }

    def evalPredicate(pred: Predicate): Option[Boolean] = {
      pred match {
        case AND(_) | OR(_) => evalCompoundPredicate(pred.asInstanceOf[CompoundPredicate])
        // case NOT(p) => for {b <- e(p)} yield !b
        case EQ(l, r)    => evalEqualities(l, r, _ == _)
        case NEQ(l, r)   => evalEqualities(l, r, _ != _)
        case GR(l, r)    => evalEqualities(l, r, _ > _)
        case LE(l, r)    => evalEqualities(l, r, _ < _)
        case GREQ(l, r)  => evalEqualities(l, r, _ >= _)
        case LEQ(l, r)   => evalEqualities(l, r, _ <= _)
        case CUSTOM(l, r, f)   => evalEqualities(l, r, f)
        case AlwaysTrue  => Some(true)
        case AlwaysFalse => Some(false)
        case p           => throw new IllegalArgumentException(s"Unknown predicate: $p")
      }
    }
    def evalCompoundPredicate(cp: CompoundPredicate): Option[Boolean] = {
      val inner = cp.p.flatMap(evalPredicate)
      if (!allowPartialStates && inner.size < cp.p.size)
        None // tried to evaluate a variable that was not part of the state map.
      else
        cp match {
          case AND(p) => Some(!inner.contains(false))
          case OR(p)  => Some(inner.contains(true))
        }
    }

    evalPredicate(this)
  }

}

sealed trait CompoundPredicate     extends Predicate { val p: List[Predicate] }
case class AND(p: List[Predicate]) extends CompoundPredicate
case class OR(p: List[Predicate])  extends CompoundPredicate
//case class NOT(p: Predicate) extends Predicate
case object AlwaysTrue  extends Predicate
case object AlwaysFalse extends Predicate

case class EQ(l: String, r: Any)   extends Predicate
case class NEQ(l: String, r: Any)  extends Predicate
case class GR(l: String, r: Any)   extends Predicate
case class LE(l: String, r: Any)   extends Predicate
case class GREQ(l: String, r: Any) extends Predicate
case class LEQ(l: String, r: Any)  extends Predicate
case class CUSTOM(l: String, r: Any, f: (Any, Any) => Boolean)  extends Predicate

// Secondary constructors to handle simplify lists
object AND { def apply(p: Predicate*) = new AND(p.toList) }
object OR  { def apply(p: Predicate*) = new OR(p.toList)  }
