/*
 * Learning Automata for Supervisory Synthesis
 *  Copyright (C) 2019
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package modelbuilding.solvers

import grizzled.slf4j.Logging
import modelbuilding.algorithms.EquivalenceOracle.Wmethod
import modelbuilding.algorithms.LStar.LStar
import modelbuilding.core.{Automata, _}

/** Implementation of the Lstar to learn a monolithic supervisor as described in
  * "Synthesis of Supervisors for Unknown Plant Models Using Active Learning" Farooqui et. al.
  *
  * @param _sul takes a monolithic model with a specification.
  */
class LStarSuprSolver(_sul: SUL) extends BaseSolver with Logging {

  assert(_sul.specification.isDefined, "Specs need to be defined")

  val _model = _sul.model
  assert(_sul.specification.isDefined, "Specification automaton must exist")

  val specs = _sul.specification.get
  specs.usePlantifiedSpec
  specs.addSynchronizedSpec
  info(s"using plantified specs")
  val teacher  = _sul
  val alphabet = _model.alphabet + Alphabet(Symbol(tau))

  //The commented part is applicable for modular learning. When that comes in. FOr now stick to monolithic learning.
  /*
  def getRequiredModules(m:ModularModel,spec:automata.Automaton):Map[String,Alphabet]={
    m.eventMapping filter {
      case (k: String, v: Alphabet) => v.events.exists(x => x.getCommand.isInstanceOf[Uncontrollable] && spec.getAlphabet.contains(x.toString))
    }
  }
  def aggregateModulesForModel(m:ModularModel,sp:Set[automata.Automaton]): Map[automata.Automaton, Alphabet] = {
    sp.map {
      s =>
        val specAlphabet = s.getAlphabet
        //This should work for the base case -- we get a supervisor, but not a supremal controllable supervisor
        def selectedAlphabet = getRequiredModules(m,s).values.toSet
        val selAlphabet = selectedAlphabet.foldLeft(Set.empty[Symbol]) {
          (acc, i) => acc union i.events
        } union m.alphabet.events.filter(a => specAlphabet.contains(a.toString))

        println(s"Selected alphabet: ${s->selAlphabet}")
        //Dirty hack, Alphabet does not take a set as is.
        val newAlphabet = new Alphabet(selAlphabet) + Alphabet(Symbol(tau))
        s -> newAlphabet
    }.toMap
  }

  val alphabetMapping: Map[automata.Automaton, Alphabet] = if(_model.isModular){
      val model = _model.asInstanceOf[ModularModel]
     aggregateModulesForModel(model,specs.values.toSet)
    }else{
      //assert(specs.size==1,"for monolithic case we allow just one spec")
      val alphabet=_model.alphabet + Alphabet(Symbol(tau))
    Map(specs.head._2->alphabet)
    }

  val runner = specs.map{s=>

    val alphabet = alphabetMapping(s._2)
      info(s"learning for ${s._1} with alphabet $alphabet")
   */
//TODO: The Wmethod does not have information about the speck, this will not work for supervisors.....
  val runner = new LStar(
    teacher,
    Some(specs.syncSpecName),
    alphabet,
    Wmethod(alphabet, 50)
  ).startLearning().removeTauAndDump
  //}.toMap

  override def getAutomata: Automata = {
    Automata(Set(runner))
  }
}
