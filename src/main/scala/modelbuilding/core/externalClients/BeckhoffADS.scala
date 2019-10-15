/*package modelbuilding.core.externalClients

import grizzled.slf4j.Logging
import modelbuilding.core.StateMap
import nl.vroste.adsclient.{
  AdsClient,
  AdsCodecs,
  AdsConnectionSettings,
  AdsNotification,
  AmsNetId
}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{Consumer, Observable}
import scodec.Codec

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success}

class BeckhoffADS extends AdsCodecs with Logging {
  private var activeState: Map[String, Any] = Map.empty

  private val targetString = "10.211.55.3.1.1"
  private val targetPort   = 801
  private val sourceString = "10.211.55.3.10.10"
  private val sourcePort   = 123
  private val hostname     = "localhost"
  private val port         = 851

  private var adsClient: Option[AdsClient] = None

  def getAdsClient = {
    if (adsClient.isEmpty)
      connect(targetString, targetPort, sourceString, sourcePort, hostname, port).get
    else adsClient.get
  }

  def connect(
      targetString: String,
      targetPort: Int,
      sourceString: String,
      sourcePort: Int,
      hostname: String,
      port: Int
    ): Option[AdsClient] = {
    val settings = AdsConnectionSettings(
      AmsNetId.fromString(targetString),
      targetPort,
      AmsNetId.fromString(sourceString),
      sourcePort,
      hostname,
      port
    )
    val clientT: Task[AdsClient] = AdsClient.connect(settings)
    adsClient = Some(Await.result(clientT.runToFuture, 10 seconds))

    adsClient
  }

  def updActiveNodes[T](k: String, v: T) = {
    activeState + (k -> v)
  }

  /**
 * I guess we can subscribe to only one type at a time, for now.
 * @param nodes
 * @tparam T
 * @return
 */
  def subscribeTo[T](
      nodes: List[(String, Codec[T])],
      callback: (String, T) => Unit = updActiveNodes
    ) = {
    val tasks: Map[String, Task[Unit]] = nodes.map { n =>
      subscribeTo(n._1, n._2)
    }.toMap
  }

  def subscribeTo[T](
      node: String,
      codec: Codec[T],
      callback: (String, T) => Unit = updActiveNodes
    ) = {
    node -> getAdsClient
      .notificationsFor(node, codec)
      .consumeWith(Consumer.foreach(x => callback(node, x.value)))
  }

  def writeTo[T](node: String, v: T, codec: Codec[T]) = {
    val t = getAdsClient.write(node, v, codec).runToFuture
    Await.ready(t, 5 seconds).value.get match {
      case Failure(exception) =>
      case Success(value)     => info(s"wrote $node with value $v")
    }

  }

  def getState: StateMap = StateMap(activeState)

  implicit def stringToCodec(s: String) = {
    s.toLowerCase match {
      case "int"     => AdsCodecs.int
      case "string"  => AdsCodecs.string
      case "boolean" => AdsCodecs.bool
      case _         => throw new NoSuchElementException(s"codec $s is not implemented")
    }
  }

  def setState(st: StateMap) = {
    st.states.foreach(k => writeTo(k._1, k._2, k._2.getClass.toString))
  }
}

object BeckhoffADS {
  def apply(): BeckhoffADS = new BeckhoffADS()
}
 */
