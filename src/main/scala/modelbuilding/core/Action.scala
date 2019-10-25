package modelbuilding.core

trait Action {
  val key: String

  def next(s:StateMap): StateMap =
  {
    this match {
      case TauAction => s
      case x if !(s.states contains x.key) => s
      case Assign(k: String,v: Any) => s.next(k,v)
      case AssignInMap(k:String,m:String,v:Any) => s.next(k,s.getOrElse(k,Map(m->v)).asInstanceOf[Map[String,Any]] ++ Map(m->v))
      case Incr(k,v) =>s.next(k, s.getOrElse(k,0).asInstanceOf[Int] + v.asInstanceOf[Int])
      case Decr(k,v) =>s.next(k,s.getOrElse(k,0).asInstanceOf[Int] - v.asInstanceOf[Int])
      case Toggle(k) => s.next(k, !s.getKey(k).get.asInstanceOf[Boolean])
      case ToggleWithValues(k:String,v:(Any,Any)) => s.next(k,if(s.getKey(k).get == v._1) v._2 else v._1 )
    }
  }
}


case class Assign(key:String, value: Any) extends Action
case class AssignInMap(key:String, internalKey:String,value:Any) extends Action
case class Incr(key:String, value:AnyVal) extends Action
case class Decr(key:String, value:AnyVal) extends Action
case class Toggle(key:String) extends Action
case class ToggleWithValues(key:String,value:(Any,Any)) extends Action

case object TauAction extends Action {val key = ""}
case object ResetAction extends Action {val key = ""}

