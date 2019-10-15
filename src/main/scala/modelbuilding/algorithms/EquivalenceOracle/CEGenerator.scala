package modelbuilding.algorithms.EquivalenceOracle

import grizzled.slf4j.Logging
import modelbuilding.algorithms.LStar.ObservationTable
import modelbuilding.core.Grammar

trait CEGenerator extends Logging {

  def findCE(t: ObservationTable): Either[Grammar, Boolean]
}
