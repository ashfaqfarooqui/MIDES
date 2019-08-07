package modelbuilding.core

case class StateMap(name: String = "State", state: Map[String, Any]) {

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

  override def toString: String = state.map(x=>(s"(${x._1},${x._2})")).toString()
}
