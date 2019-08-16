package modelbuilding.core


object State {
  def apply(s: AnyVal) = new State(s.toString)
}
case class State(s: String){
  override def toString = s
}


object StateSet {
  def apply(in: String*) = new StateSet(in.toSet)
}
case class StateSet(states: Set[String]) {
  def +(that: StateSet): StateSet = StateSet(this.states union that.states)
  override def toString: String = s"StateSet(${states.mkString(", ")})"
}


object StateMap {
  def apply(in: (String, Any)*) = new StateMap(state = in.toMap)
  def apply(states: Map[String, Any]) = new StateMap(state = states)
  def apply(states: Map[String, Any], specs: Map[String, String]) = new StateMap(state = states, specs = specs)
}
case class StateMap(name: String = "State", state: Map[String, Any], specs: Map[String, String] = Map.empty[String,String]) {

  def removeKeys(key:Set[String]) = StateMap(state=this.getState.filterKeys( !key.contains(_)))
  def getState:Map[String,Any] = state
  def getOrElse(key:String,default:Any): Any = this.state.getOrElse(key,default)
  def inState(key:String): Boolean = this.state.contains(key)
  def getKey(key:String): Option[Any] = this.state.get(key)
  def get(key:String):Either[String,Any] = {
    if(inState(key)){
      Right(getKey(key).get)
    }
    else{
      Left("Key does not exist")
    }
  }
  def next(sMap : (String,Any)) = this.copy(state = this.state + sMap)
  def next(sMap: Map[String,Any]) = this.copy(state = this.state ++ sMap)

  def equals(o: StateMap): Boolean = this.getState.forall{p => o.getState(p._1) == p._2}

  override def toString: String = {
    "StateMap(States(" + state.map(v => s"${v._1}=${v._2}").mkString(",") + ")" +
      (if (specs.nonEmpty) ", Specs(" + specs.map(v => s"${v._1}=${v._2}").mkString(",") + "))" else ")")
  }
}
