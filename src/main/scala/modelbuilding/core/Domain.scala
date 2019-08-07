package modelbuilding.core

sealed abstract trait Grammar{
def +(b:Grammar):Grammar

  def length:Int = this match {
    case w:Word => w.pf.length + w.w.length
    case s:Symbol => 1
  }

  override def toString: String = this match {
    case w:Word => w.getSequence.map(_.toString).mkString(",")
    case s:Symbol => s.s.toString.mkString("")
  }
  def getAllPrefixes = {
    def createPrefixList(g:Grammar):Set[Grammar] ={
      val prefixes = g match{
        case w:Word => w.getPrefix match {
          case Some(x) => Set(x) ++ createPrefixList(x)
          case _ => Set.empty
        }
        case s:Symbol => Set(s)
      }
      prefixes ++ Set(g)

    }
    createPrefixList(this)
  }

  def getSequenceAsList: List[Symbol]={
    this match {
      case w:Word => w.getSequence
      case s:Symbol => List(s)
    }
  }
  def getSequenceAsString: List[String]={
    this match {
      case w:Word => w.getSequence.map(_.s.toString)
      case s:Symbol => List(s.s.toString)
    }
  }
}
case class Symbol(s:Command) extends Grammar{
  //override def toString: String = s.toString.mkString("")

  def getCommand = s

  override def equals(o: Any)= o match{
    case that: Symbol =>that.s == this.s
    case that: Word => (that.w == this.s) && that.pf.isEmpty
    case _ => false
  }
   def +(b:Grammar):Grammar={
    (this,b) match {
      case (a:Symbol,b:Symbol) => Word(List(a),b)
      case (a:Symbol,b:Word) => Word(a :: b.pf,b.w)
    }
   }
}
case class Word(pf:List[Symbol], w:Symbol) extends Grammar{
  override def equals(o: Any) = o match{

    case that:Word =>
      ( that.w equals w) && (that.pf == this.pf)

    case that:Symbol =>
      (that.s equals w) && this.pf.isEmpty
  }
 def +(b:Grammar):Word={
    (this,b) match {
      case (a:Word,b:Word) => Word(a.getSequence ::: b.pf,b.w)
      case (a:Word,b:Symbol) => Word(a.getSequence,b)
    }
 }
  def getSequence:List[Symbol] = pf:::List(w)//w :: pf
  def getPrefix : Option[Grammar]={
    if(pf.isEmpty)
      None
    else
      if(pf.length == 1){
        Some(pf.head)
      }
      else
        Some(Word(pf.init,pf.last))
  }



  //override def toString: String = getSequence.mkString(",")
}

case class Language(l:Set[Grammar])
case class Transition(source: StateMap, target: StateMap, event: Command)


