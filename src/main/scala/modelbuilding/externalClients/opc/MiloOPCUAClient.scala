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

package modelbuilding.externalClients.opc

import grizzled.slf4j.Logging
import modelbuilding.core.StateMap
import modelbuilding.helpers.ConfigHelper
import org.eclipse.milo.opcua.stack.client.DiscoveryClient

import scala.util.{Failure, Success, Try}

// Milo
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.UaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder
import org.eclipse.milo.opcua.sdk.client.api.nodes.Node
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.{
  UaMonitoredItem,
  UaSubscription
}
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode
import org.eclipse.milo.opcua.stack.core.types.builtin._
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned._
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned._
import org.eclipse.milo.opcua.stack.core.types.enumerated._
import org.eclipse.milo.opcua.stack.core.types.structured._
import org.eclipse.milo.opcua.stack.core.{
  AttributeId,
  BuiltinDataType,
  Identifiers,
  Stack
}

// Java support
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiConsumer

import scala.collection.JavaConverters._

case class StateUpdate(activeState: Map[String, Any])

object MiloOPCUAClient {
  def apply(): MiloOPCUAClient = new MiloOPCUAClient()
  // call this at the end
  def destroy() = {
    Stack.releaseSharedResources()
  }
}

class MiloOPCUAClient extends Logging {
  var client: UaClient                            = null
  var clientHandles: AtomicLong                   = new AtomicLong(1L);
  var availableNodes: Map[String, UaVariableNode] = Map()
  var activeState: Map[String, Any]               = Map()
  var currentTime: org.joda.time.DateTime         = org.joda.time.DateTime.now
  var subscriptions: Set[UaSubscription]          = Set()

  def disconnect() = {
    if (client != null) {
      // un-monitor
      subscriptions.foreach(s => {
        val monitored = s.getMonitoredItems()
        s.deleteMonitoredItems(monitored).get()
      })
      subscriptions = Set()
      availableNodes = Map()
      client.disconnect().get()
      client = null
    }
  }

  def isConnected = client != null

  def connect(url: String = ConfigHelper.url): Boolean = {
    try {
      val configBuilder: OpcUaClientConfigBuilder = new OpcUaClientConfigBuilder();
      val endpoints = DiscoveryClient
        .getEndpoints(url)
        .get()
        .asScala
        .toList; //UaTcpStackClient.getEndpoints(url).get().toList
      val endpoint: EndpointDescription =
        endpoints.filter(e => e.getEndpointUrl.equals(url)) match {
          case xs :: _ => xs
          case Nil     => throw new Exception("no desired endpoints returned")
        }
      configBuilder.setEndpoint(endpoint);
      configBuilder.setApplicationName(LocalizedText.english("OPC UA client"));
      configBuilder.build();
      client = OpcUaClient.create(configBuilder.build())
      client.connect().get()
      // periodically ask for the server time just to keep session alive
      setupServerTimeSubsciption()
      populateNodes(Identifiers.RootFolder)
      true
    } catch {
      case e: Exception =>
        //println("OPCUA - " + e.getMessage())
        client = null;
        false
    }
  }

  // for now we only support Variable nodes with String identifiers
  // and identifiers need to be unique
  def populateNodes(browseRoot: NodeId): Unit = {
    def nodes: List[Node] =
      client.getAddressSpace().browse(browseRoot).get().asScala.toList
    nodes.map { x =>
      val nodeid = x.getNodeId.get()
      if (x.getNodeClass().get() == NodeClass.Variable && nodeid
            .getType() == IdType.String) {
        val identifier = nodeid.getIdentifier().toString
        if (!availableNodes.exists(_._1 == identifier))
          availableNodes += (identifier -> x.asInstanceOf[UaVariableNode])
        else {}
        //println(s"OPCUA - Node ${identifier} already exists, skipping!")
      }
      populateNodes(nodeid)
    }
  }

  def getCurrentTime: org.joda.time.DateTime = currentTime

  def setupServerTimeSubsciption(): Unit = {
    val subscription = client.getSubscriptionManager.createSubscription(100).get()
    val node = client
      .getAddressSpace()
      .createVariableNode(Identifiers.Server_ServerStatus_CurrentTime);

    val n = node.getNodeId().get()
    def readValueId =
      new ReadValueId(n, AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE)
    def parameters =
      new MonitoringParameters(
        uint(clientHandles.getAndIncrement()),
        100.0,
        null,
        uint(10),
        true
      )
    val requests = List(
      new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters)
    )
    def onItemCreated = new BiConsumer[UaMonitoredItem, Integer] {
      def accept(item: UaMonitoredItem, id: Integer): Unit = {}
    }
    def onSubscription = new BiConsumer[UaMonitoredItem, DataValue] {
      def accept(item: UaMonitoredItem, dataValue: DataValue): Unit = {
        val epoch = dataValue.getValue().getValue().asInstanceOf[DateTime].getJavaTime()
        currentTime = new org.joda.time.DateTime(epoch)
      }
    }
    subscription.createMonitoredItems(
      TimestampsToReturn.Both,
      requests.asJava,
      onItemCreated
    )
    subscriptions += subscription
  }

  def subscribeToNodes(
      identifiers: List[String],
      samplingInterval: Double = 100.0
    ): Unit = {
    val subscription =
      client.getSubscriptionManager.createSubscription(samplingInterval).get()

    val filtered = identifiers.filter(availableNodes.contains(_))
    // println(filtered)
    identifiers.filterNot(availableNodes.contains(_)).foreach { s =>
      println("OPCUA - key does not exist! skipping: " + s)
    }

    val requests = filtered.map { i =>
      val node = availableNodes(i)
      val n    = node.getNodeId().get()
      def readValueId =
        new ReadValueId(n, AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE)
      def parameters =
        new MonitoringParameters(
          uint(clientHandles.getAndIncrement()),
          samplingInterval,
          null,
          uint(10),
          true
        )
      activeState = activeState + (i -> node.getValue.get())
      new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters)
    }

    def onItemCreated = new BiConsumer[UaMonitoredItem, Integer] {
      def accept(item: UaMonitoredItem, id: Integer): Unit = {
        item.setValueConsumer(onSubscription)
      }
    }

    def onSubscription = new BiConsumer[UaMonitoredItem, DataValue] {
      def accept(item: UaMonitoredItem, dataValue: DataValue): Unit = {
        val nodeid = item.getReadValueId.getNodeId.getIdentifier().toString
        val value  = fromDataValue(dataValue)
        // println("OPCUA - " + nodeid + " got " + spval)
        activeState += (nodeid -> value)
      }
    }
    subscription.createMonitoredItems(
      TimestampsToReturn.Both,
      requests.asJava,
      onItemCreated
    )
    subscriptions += subscription
  }

  def fromDataValue(dv: DataValue): Any = {
    val v      = dv.getValue().getValue()
    val typeid = dv.getValue().getDataType().get()
    val c      = BuiltinDataType.getBackingClass(typeid)
    c match {
      case q if q == classOf[java.lang.Integer] => v.asInstanceOf[Int]
      case q if q == classOf[UByte]             => v.asInstanceOf[UByte].intValue()
      case q if q == classOf[java.lang.Short] =>
        v.asInstanceOf[java.lang.Short].intValue()
      case q if q == classOf[UShort]            => v.asInstanceOf[UShort].intValue()
      case q if q == classOf[java.lang.Long]    => v.asInstanceOf[java.lang.Long].intValue()
      case q if q == classOf[String]            => v.asInstanceOf[String]
      case q if q == classOf[ByteString]        => v.asInstanceOf[ByteString].toString
      case q if q == classOf[java.lang.Boolean] => v.asInstanceOf[Boolean]
      case q if q == classOf[java.lang.Double] =>
        v.asInstanceOf[java.lang.Double].doubleValue()
      case _ => println(s"need to add type: ${c}"); "fail"
    }
  }

  def toDataValue(value: Any, targetType: NodeId): Try[DataValue] = {
    Try {
      val c = BuiltinDataType.getBackingClass(targetType)
      //info("milo backing type: " + c.toString)
      c match {
        case q if q == classOf[java.lang.Integer] =>
          new DataValue(new Variant(value.asInstanceOf[Int]))
        case q if q == classOf[UByte] =>
          new DataValue(new Variant(ubyte(value.asInstanceOf[Byte])))
        case q if q == classOf[UShort] =>
          new DataValue(new Variant(ushort(value.asInstanceOf[Short])))
        case q if q == classOf[java.lang.Short] =>
          new DataValue(new Variant(value.asInstanceOf[Short]))

        case q if q == classOf[String] =>
          new DataValue(new Variant(value.asInstanceOf[String]))
        case q if q == classOf[ByteString] =>
          new DataValue(new Variant(ByteString.of(value.asInstanceOf[String].getBytes())))
        case q if q == classOf[java.lang.Boolean] =>
          new DataValue(new Variant(value.asInstanceOf[Boolean]), null, null, null)
        case q if q == classOf[java.lang.Double] =>
          new DataValue(new Variant(value.asInstanceOf[Double]))
        case _ => println(s"need to add type: ${c}"); new DataValue(new Variant(false))
      }
    }
  }

  def write(nodeIdentifier: String, value: Any): Boolean = {
    availableNodes.get(nodeIdentifier) match {
      case Some(n) =>
        val typeid = n.getDataType.get()
        /*val typeid2 = n.getNodeId().get()
        println(s"The typeid in the case is $typeid2")
        val typeid3 = n.getNodeId().get().getIdentifier
        println(s"The typeid should maybe be $typeid3")*/
        val dv = toDataValue(value, typeid)
        //info("trying to write: " + dv)

        dv match {
          case Failure(exception) =>
            throw new Exception(
              s"could not convert datavalue $dv, throwing exception $exception"
            )
          case Success(value) =>
            val w = client.writeValue(n.getNodeId.get, value).get
            if (w.isGood) {
              //info("OPCUA - value written")
              true
            } else {
              //println(s"OPCUA - Failed to write to node ${nodeIdentifier} - probably wrong datatype, should be: " + typeid)
              false
            }
        }
      /*dv.map { d =>
          if (client.writeValue(n.getNodeId().get(), d).get().isGood()) {
            println("OPCUA - value written")
            true
          }
          else {
            println(s"OPCUA - Failed to write to node ${nodeIdentifier} - probably wrong datatype, should be: " + typeid)
            false
          }
        }.getOrElse(false)*/
      case None => println(s"OPCUA No such node ${nodeIdentifier}"); false
    }
  }

  def getState: StateMap = StateMap(activeState)

  def setState(st: StateMap) = {
    st.states.foreach(k => write(k._1, k._2))
  }

  def getAvailableNodes(): Map[String, String] = {
    availableNodes.map {
      case (i, n) =>
        val t = n.getDataType().get()
        val c = BuiltinDataType.getBackingClass(t)
        (i, c.getSimpleName)
    }.toMap
  }
}
