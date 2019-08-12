package modelbuilding.solvers
import SupremicaStuff.SupremicaHelpers
import grizzled.slf4j.Logging
import modelbuilding.core.modelInterfaces.ModularModel.Module
import modelbuilding.core.{Alphabet, Automata, StateMap, StateMapTransition, Symbol}
import modelbuilding.core.modelInterfaces.{Model, ModularModel, MonolithicModel}
import modelbuilding.solvers.ModularSupSolver._
import net.sourceforge.waters.subject.module.ModuleSubject
import org.supremica.automata

import scala.collection.mutable

object ModularSupSolver{
  def aggregateModulesForModel(m:ModularModel,sp:Set[automata.Automaton]) = {
    sp.map {
      s =>
        val specAlphabet = s.getAlphabet
        //This should work for the base case -- we get a supervisor, but not a supremal controllable supervisor
        def selectedAlphabet = m.eventMapping filter {
          case (k: String, v: Set[Symbol]) => v.exists(x => specAlphabet.contains(x.toString))
        }

        val selAlphabet = Alphabet(selectedAlphabet.values.toSet.foldLeft(Set.empty[Symbol]) {
          (acc, i) => acc union i.events
        } union m.alphabet.a.filter(a => specAlphabet.contains(a.toString)))
        s -> selAlphabet
    }.toMap
  }

}

class ModularSupSolver(_model:Model) extends BaseSolver with SupremicaHelpers with  Logging {
  override def getAutomata: Automata = ???

  assert(_model.specFilePath.isDefined, "modelbuilder.solver.ModularSupSolver requires a specification.")
  assert(_model.isModular, "modelbuilder.solver.ModularSupSolver requires a modular model.")

  info("Initializing SupSolver")
  override val mModule: ModuleSubject = ReadFromWmodFile(_model.specFilePath.get).get
  val specs = getSupremicaAutomataFromWaters(mModule).get.filter(_.isSpecification).toSet



  specs.foreach(s=>info(s"Read spec for ${s.getName}"))

  val model = _model.asInstanceOf[ModularModel]

  val globalQueue:mutable.Queue[StateMap] = ???
  val moduleMapping = aggregateModulesForModel(model,specs)
  private var moduleTransitions: Map[Module, Set[StateMapTransition]] = moduleMapping.map(_._1.getName -> Set.empty[StateMapTransition]).toMap



}
