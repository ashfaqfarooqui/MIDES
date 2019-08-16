package modelbuilding.algorithms.EquivalenceOracle

import SupremicaStuff.SupremicaHelpers
import grizzled.slf4j.Logging
import modelbuilding.algorithms.LStar.ObservationTable
import modelbuilding.core.Grammar
import net.sourceforge.waters.subject.module.ModuleSubject

class Wmethod extends CEGenerator with SupremicaHelpers with Logging {
  override def findCE(t: ObservationTable): Either[Grammar, Boolean] = ???

  override val mModule: ModuleSubject = _
}
