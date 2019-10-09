package modelbuilding.core

trait ThreeStateOperation extends TwoStateOperation {
  val postGuards: Map[Command, Predicate]
  val postActions: Map[Command, List[Action]]

}
trait TwoStateOperation {
  val guards: Map[Command, Predicate]
  val actions: Map[Command, List[Action]]

}
