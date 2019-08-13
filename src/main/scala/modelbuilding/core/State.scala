package modelbuilding.core


object State {
  def apply(s: AnyVal) = new State(s.toString)
}
case class State(s: String)


object StateSet {
  def apply(in: String*) = new StateSet(in.toSet)
}
case class StateSet(states: Set[String])


object StateMap {
  def apply(in: (String, Any)*) = new StateMap(state = in.toMap)
  def apply(state: Map[String, Any]) = new StateMap(state = state)
}
case class StateMap(name: String = "State", state: Map[String, Any]) {





  val x: StateSet = StateSet("a","2")



  def getState:Map[String,Any] = state
  def getOrElse(key:String,default:Any) = this.state.getOrElse(key,default)
  def inState(key:String) = this.state.contains(key)
  def getKey(key:String) = this.state.get(key)
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

  override def toString: String = "(" + state.map(v => s"${v._1}=${v._2}").mkString(",") + ")"
}
