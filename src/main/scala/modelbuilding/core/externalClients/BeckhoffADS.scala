package modelbuilding.core.externalClients

import nl.vroste.adsclient.{AdsClient, AdsCodecs, AdsConnectionSettings, AdsNotification, AmsNetId}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{Consumer, Observable}
import scodec.Codec

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Success

class BeckhoffADS extends AdsCodecs {
  private val targetString = ""
  private val targetPort = 888
  private val sourceString = ""
  private val sourcePort = 888
  private val hostname = "localhost"
  private val port = 33333


  private var adsClient: Option[AdsClient] = None

  def getAdsClient = {
    if(adsClient.isEmpty) connect(targetString,targetPort,sourceString,sourcePort,hostname,port).get
    else adsClient.get
  }
  def connect(targetString:String,targetPort:Int,sourceString:String,sourcePort:Int,hostname:String,port:Int)={
    val settings = AdsConnectionSettings(AmsNetId.fromString(targetString), targetPort, AmsNetId.fromString(sourceString), sourcePort, hostname,port)
    val clientT: Task[AdsClient] = AdsClient.connect(settings)
    adsClient = Some(Await.result(clientT.runToFuture,10 seconds))

    adsClient
  }

  /**
   * I guess we can subscribe to only one type at a time, for now.
   * @param nodes
   * @tparam T
   * @return
   */
  def subscribeTo[T](nodes: List[(String,Codec[T] )], callback: T=>Unit)={
    val tasks:Map[String,Task[Unit]] = nodes.map { n =>
      n._1 -> getAdsClient.notificationsFor(n._1, n._2)
        .consumeWith(Consumer.foreach(x=>callback(x.value)))
    }.toMap
  }

  def writeTo[T](node:String, codec:Codec[T])={

  }


}

object BeckhoffADS {
  def apply(): BeckhoffADS = new BeckhoffADS()
}
