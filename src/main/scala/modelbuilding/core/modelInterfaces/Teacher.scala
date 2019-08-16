package modelbuilding.core.modelInterfaces

import modelbuilding.algorithms.EquivalenceOracle.CEGenerator
import modelbuilding.algorithms.LStar.ObservationTable
import modelbuilding.core.Grammar

trait Teacher {

  def isMember(sequence: Grammar): Int

  def isHypothesisTrue(t: ObservationTable, ceGenerator: CEGenerator) :  Either[Grammar, Boolean] ={

    val ce = ceGenerator.findCE(t)
    //    debug(s"Eq: $ce returned")
    ce
  }

}
