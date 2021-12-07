package modelbuilding.models.WeldingRobots

import modelbuilding.core._
import modelbuilding.core.interfaces.simulator.TimedCodeSimulator

import scala.util.Random

object WeldingRobotsSimulation {
  def global_behavior1(n_robots: Int, n_tasks: Int, n_independent: Int = 1): Unit = {

    // should define the precedence relation between the global event and all other tasks.

  }
}

class WeldingRobotsSimulation(
                               n_robots: Int,
                               n_tasks: Int,
                               seed: Int = 0,
                               durationMax: Int = 1,
                               globalBehavior: Any = WeldingRobotsSimulation.global_behavior1(1,1),
                               dim: (Int,Int) = (5,5) // dimensions of robot working area
                              ) extends TimedCodeSimulator {

  private val rand = new Random(seed)
    assert(n_tasks <= 31, "The bitvectors are currently made up by Int and handle a maximum of 31 tasks/robot.")


  val n_independent = 1


  // An attempt to make the bitvectors dynamic.
  //  def toState(n: Int): AnyVal = n_tasks match {
  //    case x if x <= 8 => n.toByte
  //    case x if x <= 16 => n.toShort
  //    case x if x <= 32 => n
  //    case _ => n.toLong
  //  }
  //  println(state(1).getClass)


  // Specifies the duration of each task (not including movement of the robot)
  val durationTask: Double = durationMax
  val durationGlobalEvent: Double = durationTask*2

  // For each robot, assign `n_tasks` at random locations within the specified dimensions. This is used to determine the travel time between tasks
  val _positions = (1 to n_robots).map(_ => 0 +: rand.shuffle((1 until dim._1*dim._2).toList).take(n_tasks))
  val positions: Vector[Vector[(Int,Int)]] = _positions.map(_.map(p => (p/dim._1,p%dim._1)).toVector).toVector

  override val initState: StateMap               =
    StateMap(
      (1 to n_robots).flatMap(r => Map[String,Any](s"r_${r}_l" -> 0, s"r_${r}_d" -> false, s"r_${r}_t" -> 0)).toMap +
      ("s" -> false)
    )
  val goalState: StateMap =
    StateMap(
      (1 to n_robots).flatMap(r => Map[String,Any](s"r_${r}_l" -> 0, s"r_${r}_d" -> true, s"r_${r}_t" -> (math.pow(2,n_tasks)-1))).toMap +
        ("s" -> true)
    )
  override val goalStates: Option[Set[StateMap]] = Some(Set(goalState))
  override val goalPredicate: Option[Predicate]  = None

  def BIT(v: String, i: Int): Predicate = CUSTOM(v,i,(x:Any, y: Any) => (x.asInstanceOf[Int] & 1<<(y.asInstanceOf[Int]-1)) != 0)
  def NBIT(v: String, i: Int): Predicate = CUSTOM(v,i,(x:Any, y: Any) => (x.asInstanceOf[Int] & 1<<(y.asInstanceOf[Int]-1)) == 0)
  def BITS(v: String, m: Int): Predicate = CUSTOM(v,m, (x:Any, y: Any) => (~x.asInstanceOf[Int] & y.asInstanceOf[Int]) == 0)
  def NBITS(v: String, m: Int): Predicate = CUSTOM(v,m, (x:Any, y: Any) => (x.asInstanceOf[Int] & y.asInstanceOf[Int]) == 0)

  val halfway = (n_tasks+1-n_independent)/2


  override val guards: Map[String, Predicate] =
    // Example: "c1" -> EQ(cat, R0),
    (1 to n_robots).flatMap(r =>
      Map[String,Predicate](s"e_${r}_0" -> NEQ(s"r_${r}_l", 0))
        ++ (1 to n_independent).map(t => s"e_${r}_${t}" -> AND(NBIT(s"r_${r}_t", t), NEQ(s"r_${r}_l", t)))
        ++ (n_independent+1 to n_independent+halfway).map(t => s"e_${r}_${t}" -> AND(NBIT(s"r_${r}_t", t), NEQ(s"r_${r}_l", t), EQ("s", false)))
        ++ (n_independent+halfway+1 to n_tasks).map(t => s"e_${r}_${t}" -> AND(NBIT(s"r_${r}_t", t), NEQ(s"r_${r}_l", t), EQ("s", true)))
    ).toMap +
    ("s" -> AND(EQ("s",false) :: (1 to n_robots).flatMap(r => List(EQ(s"r_${r}_d", true))).toList))

  def BitSet(v: String, i: Int): Action = Transform(v, (x: Any) => x.asInstanceOf[Int] | 1 << (i-1))

  override val actions: Map[String, List[Action]] =
    (1 to n_robots).flatMap(r =>
      Map[String,List[Action]](s"e_${r}_0" -> List(Assign(s"r_${r}_l", 0), AssignPredicateValue(s"r_${r}_d", BITS(s"r_${r}_t", math.pow(2,halfway).toInt-1<<n_independent))))
        ++ (1 to n_tasks).map(t => s"e_${r}_${t}" -> List(Assign(s"r_${r}_l", t), Assign(s"r_${r}_d", false), BitSet(s"r_${r}_t", t)))
    ).toMap + ("s" -> List(Assign("s", true)))

  override def calculateDuration(t: StateMapTransition): Duration = {
    if (t.event.toString == "s") durationGlobalEvent
    else {
      val robot = t.event.toString.split("_").tail.head.toInt
      val source = t.source.states(s"r_${robot}_l").asInstanceOf[Int]
      val target = t.event.toString.split("_").tail.tail.head.toInt
      calculateDistance(robot, source, target) + (if (target != 0) durationTask else 0 )
    }
  }

  def calculateDistance(robot: Int, source: Int, target: Int): Double = {
    def distance(p1: (Int,Int), p2: (Int,Int)): Double = math.hypot((p1._1-p2._1),(p1._2-p2._2))
    distance(positions(robot-1)(source),positions(robot-1)(target))
  }

}
