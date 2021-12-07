package modelbuilding.models.WeldingRobots

import modelbuilding.core.interfaces.modeling.ModularModel
import modelbuilding.core.{Alphabet, _}

class WeldingRobots(n_robots: Int, n_tasks: Int) extends ModularModel {

  override val name: String = "WeldingRobots2"
  //val events: Seq[Command] = s +: (for { r <- 1 to robots; t <- 1 to tasks } yield event(r,t))
  //override val alphabet = Alphabet(events.toSet)

  override val modules: Set[String] = (1 to n_robots).map(r => s"R$r").toSet

  // State variables:
  // - s >> represent the status of the global task
  // - r_i_t represent the status of the tasks of robot i as a bit vector
  // - r_i_l 1 the location of the robot
  val stateString: String = "s " + (1 to n_robots).map(r => s"r_${r}_l r_${r}_d r_${r}_t").mkString(" ")
  override val states: StateSet = StateSet(stateString.split(" ").toSet)

  override def stateMapping: Map[String, StateSet] =
    (1 to n_robots).map(r => s"R$r" -> StateSet( Set(s"r_${r}_l", s"r_${r}_t") ++ (1 to n_robots).map(r => s"r_${r}_d").toSet + "s")).toMap

  override def eventMapping: Map[String, Alphabet] =
    (1 to n_robots).map(r => s"R$r" -> Alphabet((shared +: (0 to n_tasks).map(t => event(r,t))).map(Symbol).toSet)).toMap

  val events: Set[Symbol] = eventMapping.values.flatMap(a=>a.events).toSet

  override val alphabet = Alphabet(events)


}
