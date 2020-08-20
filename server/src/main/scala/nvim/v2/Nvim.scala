package nvim.v2

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import akka.actor.ActorLogging
import com.rallyhealth.weepickle.v1.WeePickle._
import com.rallyhealth.weepickle.v1._
import com.rallyhealth.weepack.v1._
import com.rallyhealth.weejson.v1.jackson.ToJson
import com.rallyhealth.weepack.v1.FromMsgPack
import com.rallyhealth.weepickle.v1.WeePickle.FromScala

object Nvim {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Nvim], remote, replies)
}

// https://github.com/msgpack-rpc/msgpack-rpc
// https://github.com/msgpack-rpc/msgpack-rpc/blob/master/spec.md

case class ConnectionFailed()
case class Request(tpe: Int, id:Int, method: String, params: List[Msg])
object Request {
  implicit val fmt = macroFromTo[Request]
}
case class Response(tpe: Int, id:Int, error: Msg, result: Msg)
case class Notification(tpe: Int, method: String, params: List[Msg])

class Nvim(remote: InetSocketAddress) extends Actor with ActorLogging { //, listener: ActorRef) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      // sender !
      log.error("connect failed")
      context.stop(self)

    case c @ Connected(remote, local) =>
      pprint.log("CONNECTED")
      // listener ! c
      val connection = sender()
      connection ! Register(self)
      context.become {
        // case notification: Notification =>
        //   pprint.log(notification)
          // val data = FromScala(notification).transform(ToMsgPack.bytes)
          //
          // val json = FromMsgPack(data.clone).transform(ToJson.string)
          // val scl = List(2,"vim_command", List("vsplit"))
          // val pak = FromScala(scl).transform(ToMsgPack.bytes)
          // val jsn = FromMsgPack(pak).transform(ToJson.string)
          // [2,"vim_command",["vsplit"]]
          // println(jsn)
          // println(json)
          // self ! ByteString(data)

        case data: ByteString =>
          log.info("write data")
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          log.info("write failed")
        // listener ! "write failed"
        case Received(data) =>
          log.info("data ack")
        // listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          // listener ! "connection closed"
          log.info("connection closed")
          context.stop(self)
      }
      // self ! Notification(0, "nvim_command", FromScala(List("vsplit")).transform(ToMsgPack.b))
  }
}
