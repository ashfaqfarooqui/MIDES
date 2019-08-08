package modelbuilding.solvers

import grizzled.slf4j.Logging
import modelbuilding.core.Automata


trait BaseSolver extends Logging {
  def getAutomata: Automata


}
