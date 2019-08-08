package modelbuilding.core

object Alphabet {

  def apply(a: AnyRef*): Alphabet =
    new Alphabet(a.toSet)
}

case class Alphabet(_a: Set[Any], includeTau: Boolean = false, includeReset: Boolean = false) {
  val events: Set[Symbol] =
    if (_a.isEmpty)
      Set.empty[Symbol]
    else
      _a.head match {
        case symbols: Symbol => _a.asInstanceOf[Set[Symbol]]
        case commands: Command => _a.asInstanceOf[Set[Command]].map(Symbol)
        case _ => throw new IllegalArgumentException("Alphabet only accept inputs of either Seq[Symbol] or Seq[Command]")
      }
  val a: Set[Symbol] = events union (if (includeTau) Set(Symbol(tau)) else Set.empty[Symbol]) union (if (includeReset) Set(Symbol(reset)) else Set.empty[Symbol])
}
