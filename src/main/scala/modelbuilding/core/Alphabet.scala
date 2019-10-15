package modelbuilding.core

object Alphabet {

  //def apply(_a: AnyRef*): Alphabet = Alphabet(false,_a)

  def apply(_a: AnyRef*): Alphabet = {
    if (_a.isEmpty) new Alphabet(Set.empty[Symbol])
    else {
      val a = _a.head match {
        case _: Symbol  => _a.toSet.asInstanceOf[Set[Symbol]]
        case _: Command => _a.toSet.asInstanceOf[Set[Command]].map(Symbol)
        case t =>
          throw new IllegalArgumentException(
            s"Alphabet only accept inputs of either Seq[Symbol] or Seq[Command], not `${t.getClass}`"
          )
      }
      new Alphabet(a)
    }
  }

}

case class Alphabet(
    _a: Set[Symbol],
    includeTau: Boolean = false,
    includeReset: Boolean = false) {
  val events: Set[Symbol] = _a union (if (includeTau) Set(Symbol(tau))
                                      else Set.empty[Symbol]) union (if (includeReset)
                                                                       Set(Symbol(reset))
                                                                     else
                                                                       Set.empty[Symbol])

  def +(that: Alphabet): Alphabet = new Alphabet(this.events union that.events)
  override def toString: String   = s"Alphabet(${events.mkString(", ")})"
}
