package modelbuilding.core

import scalax.collection.Graph
import scalax.collection.edge.{LDiEdge, LkDiEdge}
import scalax.collection.edge.Implicits._
import scalax.collection.io.dot._
import implicits._
import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import scalax.collection.GraphEdge._

case class Automaton(states: Set[State], transition: (State,Symbol) => State, A: Alphabet, iState: State, fState: Option[Set[State]],forbiddenStates:Option[Set[State]]=None){


  def createGraph={

    val edges = for{
      s <- states
      a <- A.a
    }yield {
      LkDiEdge(s,transition(s,a))(a.toString)
    }
    // val g = Graph(edges)


    //  println(s"edges = $edges")
    Graph.from(states.toList, edges)//.filter(p => p.e._1.s != "s0" && p.e._2.s != "s0" ))
  }

  def getGraphAsDot ={
    val root = DotRootGraph(directed = true,
                            id       = Some("Hypothesis"))

    def edgeTransformer(innerEdge: Graph[State,LkDiEdge]#EdgeT):
        Option[(DotGraph,DotEdgeStmt)] = innerEdge.edge match {
      case LkDiEdge(source, target, label) => label match {
        case label: String =>
          Some((root,
                DotEdgeStmt(source.toString,
                            target.toString,
                            if (label.nonEmpty) List(DotAttr("label", label.toString))
                            else                Nil)))
      }}
    val gDot = createGraph.toDot(root,edgeTransformer)
    gDot
  }


  def getNextStates(state: State): Option[Set[State]] ={
    val states = for(t <- A.a) yield (transition(state,t))
    if (states.nonEmpty) Some(states) else None
  }

  def getInitialState = iState
  def getMarkedState = fState

}
