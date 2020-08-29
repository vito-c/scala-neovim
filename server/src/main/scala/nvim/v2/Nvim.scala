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
import com.rallyhealth.weejson.v1.jackson.FromJson
import nvim.v2.msgpack.ToMsgPackRPC

// {
//   "parameters": [
//     [
//       "String",
//       "command"
//     ]
//   ],
//   "method": false,
//   "return_type": "void",
//   "name": "nvim_command",
//   "since": 1
// },
object Nvim {

  def props(
    remote: InetSocketAddress,
    replies: ActorRef
  ) =
    Props(classOf[Nvim], remote, replies)
}

// https://github.com/msgpack-rpc/msgpack-rpc
// https://github.com/msgpack-rpc/msgpack-rpc/blob/master/spec.md

case class Command(
  command: String
) {
  def notice = Notification(2, "nvim_command", List(Str(command)))
  def request(code:Int) = Request(0, code, "nvim_command", List(Str(command)))
}

case class ConnectionFailed()
case class Request(
  tpe: Int,
  id: Int,
  method: String,
  params: List[Msg]
)

object Request {
  implicit val fmt = macroFromTo[Request]
}

case class Response(
  tpe: Int,
  id: Int,
  error: Msg,
  result: Msg
)

object Response {
  implicit val fmt = macroFromTo[Response]
}

case class Notification(
  tpe: Int,
  method: String,
  params: List[Msg]
)

object Notification {
  implicit val fmt = macroFromTo[Notification]
}

class Nvim(
  remote: InetSocketAddress
) extends Actor
  with ActorLogging { //, listener: ActorRef) extends Actor with ActorLogging {

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
        case r: Request =>
          val data = FromScala(r).transform(ToMsgPackRPC.bytes)
          self ! ByteString(data)
        case notification: Notification =>
          val data = FromScala(notification).transform(ToMsgPackRPC.bytes)
          // val json = FromMsgPack(data.clone).transform(ToJson.string)
          // val scl = List(2,"vim_command", List("vsplit"))
          // val pak = FromScala(scl).transform(ToMsgPack.bytes)
          // val jsn = FromMsgPack(pak).transform(ToJson.string)
          // [2,"vim_command",["vsplit"]]
          // println(jsn)
          // println(json)
          self ! ByteString(data)

        case data: ByteString =>
          log.info("write data")
          val res = FromMsgPack(data.toArray).transform(ToJson.string)
          println("===============================")
          println(res)
          println("===============================")
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          log.info("write failed")
        // listener ! "write failed"
        case Received(data) =>
          log.info("data ack")
          val res = FromMsgPack(data.toArray).transform(ToJson.string)
          pprint.log(res)
        // listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          // listener ! "connection closed"
          log.info("connection closed")
          context.stop(self)
      }
      self ! Command("vsplit").notice
  }
}
