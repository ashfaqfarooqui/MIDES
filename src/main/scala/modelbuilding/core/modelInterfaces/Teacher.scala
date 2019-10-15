package modelbuilding.core.modelInterfaces

import modelbuilding.algorithms.EquivalenceOracle.CEGenerator
import modelbuilding.algorithms.LStar.ObservationTable
import modelbuilding.core.Grammar

trait Teacher {

  def isMember(specName: Option[String])(g: Grammar): Int

  def isHypothesisTrue(
      t: ObservationTable,
      ceGenerator: CEGenerator
    ): Either[Grammar, Boolean] = {
    val ce = ceGenerator.findCE(t)
    ce
  }

}
