///*
// * Learning Automata for Supervisory Synthesis
// *  Copyright (C) 2019
// *
// *     This program is free software: you can redistribute it and/or modify
// *     it under the terms of the GNU General Public License as published by
// *     the Free Software Foundation, either version 3 of the License, or
// *     (at your option) any later version.
// *
// *     This program is distributed in the hope that it will be useful,
// *     but WITHOUT ANY WARRANTY; without even the implied warranty of
// *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *     GNU General Public License for more details.
// *
// *     You should have received a copy of the GNU General Public License
// *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//
//package modelbuilding.solvers
//import modelbuilding.algorithms.EquivalenceOracle.Wmethod
//import modelbuilding.algorithms.KV.LearnDT
//import modelbuilding.core.{Alphabet, Automata, SUL, tau}
//
//class KVSolver(_sul: SUL) extends BaseSolver {
//  val _model   = _sul.model
//  val teacher  = _sul
//  val alphabet = _model.alphabet + Alphabet(tau)
//  val runner =
//    new LearnDT(teacher, None, alphabet, new Wmethod(teacher, alphabet, 30)).learnAutomaton
//
//  override def getAutomata: Automata = Automata(Set(runner))
//}
