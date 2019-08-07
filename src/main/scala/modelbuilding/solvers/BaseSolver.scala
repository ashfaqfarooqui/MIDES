package modelbuilding.solvers

import SupremicaStuff.SupremicaHelpers
import modelbuilding.core.Automaton
import org.supremica.automata.Automata


trait BaseSolver {
def getAutomata: Automata
}
