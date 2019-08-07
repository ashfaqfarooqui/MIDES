package core

sealed trait Predicate{

  private implicit class inequalityAttributes(l: Any) {
    def >(r: Any) = {
      (l, r) match {
        case (l: Int, r: Int) => l > r
        case (l: Float, r: Float) => l > r
        case _ => false
      }
    }

    def <(r: Any) = r > l

    def >=(r: Any) = (l > r) || (l == r)

    def <=(r: Any) = (r > l) || (l == r)

  }





    def eval(s: StateMap): Option[Boolean] = {
      def evalEqualities(l: String, r: Any, e: (Any, Any) => Boolean) = {
        s.get(l) match {
          case Right(v) => Some(e(v, r))
          case Left(p) => Some(false)
        }

      }


      def e(pred: Predicate):Option[Boolean] = {
        pred match {
          case AND(p) => Some(!p.flatMap(e(_)).contains(false))
          case OR(p) => Some(p.flatMap(e(_)).contains(true))
         // case NOT(p) => for {b <- e(p)} yield !b
          case EQ(l, r) => evalEqualities(l, r, _ == _)
          case NEQ(l, r) => evalEqualities(l, r, _ != _)
          case GR(l, r) => evalEqualities(l, r, _ > _)
          case LE(l, r) => evalEqualities(l, r, _ < _)
          case GREQ(l, r) => evalEqualities(l, r, _ >= _)
          case LEQ(l, r) => evalEqualities(l, r, _ <= _)
          case AlwaysTrue => Some(true)
          case AlwaysFalse => Some(false)
          //case Left(s) => Some(None)
        }
      }

      e(this)
    }


 }


case class AND(p:List[Predicate]) extends Predicate
case class OR(p:List[Predicate]) extends Predicate
//case class NOT(p: Predicate) extends Predicate
case object AlwaysTrue extends Predicate
case object AlwaysFalse extends Predicate


case class EQ(l:String, r:Any) extends Predicate
case class NEQ(l:String, r:Any) extends Predicate
case class GR(l:String, r:Any) extends Predicate
case class LE(l:String, r:Any) extends Predicate
case class GREQ(l:String, r:Any) extends Predicate
case class LEQ(l:String, r:Any) extends Predicate
