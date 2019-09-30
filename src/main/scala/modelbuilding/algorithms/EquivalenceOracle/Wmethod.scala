package modelbuilding.algorithms.EquivalenceOracle

import grizzled.slf4j.Logging
import modelbuilding.algorithms.LStar.ObservationTable
import modelbuilding.core.{Alphabet, Automaton, Grammar, State, Symbol}

object Wmethod{
  def apply(alphabets: Alphabet, nbrState: Int): Wmethod = new Wmethod(alphabets, nbrState)
}

class Wmethod(alphabets:Alphabet,nbrState: Int) extends CEGenerator with Logging {
  var CachecPwrA :Map[Int,Set[Grammar]] = Map(0->Set.empty[Grammar])

  private def evalString(s:Grammar,a:Automaton):Int={
    def loop(currState:State, stringToTraverse:List[Symbol]):State={

      stringToTraverse match {
        case x::xs => loop(a.transitionFunction((currState,x)),xs)
        case Nil => currState
      }
    }
    val reachedState=loop(a.getInitialState,s.getSequenceAsList)
    //debug(s"reachedstate: $reachedState")
    if(a.getMarkedState.nonEmpty && a.getMarkedState.get.contains(reachedState)){
      2
    }else {
      if(reachedState.s!="dump:"){
        1
      } else 0
    }
  }
  override def findCE(t: ObservationTable): Either[Grammar, Boolean] = {

    val P = (t.S ++ t.sa).filterNot(p=>t.getRowValues(p).get.forall(_==0))
    val W = t.E
    val h = t.getAutomata
    val A= h.alphabet.events

    CachecPwrA = CachecPwrA + (1->A.asInstanceOf[Set[Grammar]])

    def loop(n:Int, oldU:Set[Grammar]):Either[Grammar,Boolean]={
      val i = nbrState - h.states.size -n
      val cachedReply = CachecPwrA.get(i)
      lazy val U = if(cachedReply.isDefined){
        cachedReply.get
      }
      else {
        CachecPwrA =  CachecPwrA + (i -> A.flatMap(e=>CachecPwrA(i-1).map(a=>e+a)))
        CachecPwrA(i)
      }
      info(s"running for i: $i")
      if (n<=0||i>=3) {
        return Right(true)
      }

      for{
        p<-P
        w<-W
        u<-U
      }
      {
        val s = p+u+w
        val sysOp = t.isMember(s)
        val hypOp = evalString(s,h)
        debug(s"checking for ce with $p + $u + $w + ,got sys: $sysOp, and hypOp : $hypOp")
        if(sysOp != hypOp){
          return Left(s)
        }
      }
      loop(n-1,U)
    }
    loop(nbrState - h.states.size,A.asInstanceOf[Set[Grammar]])
  }
}
