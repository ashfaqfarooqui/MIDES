package modelbuilding.core

import java.io.{File, PrintWriter}

import scalax.collection.Graph
import scalax.collection.edge.{LDiEdge, LkDiEdge}
import scalax.collection.edge.Implicits._
import scalax.collection.io.dot._
import implicits._
import scalax.collection.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import scalax.collection.GraphEdge._

case class Automaton(
                      name: String,
                      states: Set[State],
                      alphabet: Alphabet,
                      transitions: Set[Transition],
                      iState: State,
                      fState: Option[Set[State]] = None,
                      forbiddenStates:Option[Set[State]]=None
                    ){

  lazy val transitionFunction: Map[(State, Symbol),State] = transitions.map( t => (t.source, t.event) -> t.target ).toMap

  def createGraph: Graph[String, LkDiEdge] ={

    val edges = transitions.map( t => LkDiEdge(t.source.s, t.target.s)(t.event.toString) )
    Graph.from(states.map(_.s).toList, edges)//.filter(p => p.e._1.s != "s0" && p.e._2.s != "s0" ))

  }

  lazy val getGraphAsDot: String = {

    val root = DotRootGraph(directed = true,
                            id       = Some(name))

    def edgeTransformer(innerEdge: Graph[String,LkDiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] =
      innerEdge.edge match {
        case LkDiEdge(source, target, label) =>
          label match {
            case label: String =>
              Some((root,
                    DotEdgeStmt(source.toString,
                                target.toString,
                                if (label.nonEmpty) List(DotAttr("label", label.toString))
                                else                Nil)))
          }
      }

    val gDot = createGraph.toDot(root,edgeTransformer)
    gDot

  }

  def createDotFile: Unit = {
    val gDot = getGraphAsDot
    val pw = new PrintWriter(new File(s"Output/$name.dot"))
    pw.write(gDot)
    pw.close
    println(s"Graph saved to $name.dot")
  }

  def getNextStates(state: State): Option[Set[State]] ={
    val states = for(e <- alphabet.a) yield transitionFunction(state,e)
    if (states.nonEmpty) Some(states) else None
  }

  def getInitialState: State = iState
  def getMarkedState: Option[Set[State]] = fState

  override def toString: String = {

    s"Automaton( $name, " +
      s"Q: (${states.map(_.s).mkString(",")}), " +
      s"A: (${alphabet.a.map(_.getCommand.toString).mkString(",")}), " +
      s"T: (${transitions.map{ case Transition(s,t,e) => s"(${s.s},${t.s},$e)" }.mkString(",")}) " +
      s"q_i: (${iState.s}) " +
      s"Q_m: ${fState match {
        case Some(fs) => "(" + fs.map(_.s).mkString(",") + ")"
        case None => "None"
      }} )"
  }

}
