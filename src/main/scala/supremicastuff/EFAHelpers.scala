package supremicastuff

import grizzled.slf4j.Logging
import net.sourceforge.waters.subject.module.ModuleSubject
import modelbuilding.core.StateMap
import modelbuilding.core.Alphabet
import modelbuilding.core.StateMapTransition
import net.sourceforge.waters.model.module.SimpleExpressionProxy
import net.sourceforge.waters.subject.module.SimpleExpressionSubject
import net.sourceforge.waters.subject.module.VariableComponentSubject
import net.sourceforge.waters.model.expr.Operator
import net.sourceforge.waters.model.module.EventDeclProxy
import net.sourceforge.waters.model.marshaller.DocumentManager
import net.sourceforge.waters.model.expr.ExpressionParser
import net.sourceforge.waters.model.compiler.CompilerOperatorTable
import net.sourceforge.waters.subject.module.ModuleSubjectFactory
import scala.collection.JavaConverters._
import java.{util => ju}
import net.sourceforge.waters.model.base.ComponentKind

trait EFAHelpers extends Logging {

  val mModule: ModuleSubject
  val mFactory: ModuleSubjectFactory
  val mOptable: CompilerOperatorTable
  val mParser: ExpressionParser
  val mDocumentManager: DocumentManager

  def createEFA(
      state: Set[StateMap],
      events: Alphabet,
      transitionMap: Set[StateMapTransition],
      istate: StateMap,
      fState: Set[StateMap],
      name: String = "hypothesis"
    ) = {

    import org.supremica.automata.ExtendedAutomaton

    val efa = new ExtendedAutomaton(name, ComponentKind.PLANT)


  }

  private def addVariable(
      name: String,
      typeOfVar: SimpleExpressionProxy,
      initValue: SimpleExpressionProxy,
      markedValues: Set[String]
    ): Option[VariableComponentSubject] = {
    val markedPredicate =
      if (markedValues.isEmpty) None
      else Some(markedValues.map(name + "==" + _).mkString("|"))
    var markedPred: SimpleExpressionSubject = null
    try {
      if (markedPredicate.isDefined) {
        markedPred = mParser
          .parse(markedPredicate.get, Operator.TYPE_BOOLEAN)
          .asInstanceOf[SimpleExpressionSubject]
      }
    } catch {
      case _: Throwable =>
        println(
          "Problem when parsing marking:\n Variable: " + name + "\n Expression: " + markedPredicate
        ); return None
    }

    val initPredicate = mFactory.createBinaryExpressionProxy(
      mOptable.getEqualsOperator(),
      mFactory.createSimpleIdentifierProxy(name),
      initValue
    )
    val accepting =
      mFactory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME)
    val marking  = mFactory.createVariableMarkingProxy(accepting, markedPred)
    val markings = ju.Collections.singletonList(marking)
    val vcs = new VariableComponentSubject(
      mFactory.createSimpleIdentifierProxy(name),
      typeOfVar,
      initPredicate,
      markings
    )

    mModule.getComponentListModifiable().add(vcs)
    Some(vcs)
  }

  def addVariable(
      name: String,
      domainValues: Set[String],
      initialValue: String,
      markedValues: Set[String]
    ): Option[VariableComponentSubject] = {
    if (!domainValues.contains(initialValue)) {
      println(
        "Problem when creating variable, initial value not in domain:\n Variable: " + name
      ); return None
    }
    if (!markedValues.filter(!domainValues.contains(_)).isEmpty) {
      println(
        "Problem when creating variable, marked values not in domain:\n Variable: " + name
      ); return None
    }
    val domain = domainValues.map(mFactory.createSimpleIdentifierProxy(_)).asJava

    addVariable(
      name,
      mFactory.createEnumSetExpressionProxy(domain),
      mFactory.createSimpleIdentifierProxy(initialValue),
      markedValues
    )
  }

}
