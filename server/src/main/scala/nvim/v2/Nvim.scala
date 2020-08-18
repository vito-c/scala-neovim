package nvim.v2

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress
import akka.actor.ActorLogging

object Nvim {
  def props(remote: InetSocketAddress, replies: ActorRef) =
    Props(classOf[Nvim], remote, replies)
}

case class ConnectionFailed()

class Nvim(remote: InetSocketAddress, listener: ActorRef) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      sender ! 
      log.info("connect failed")
      context.stop(self)

    case c @ Connected(remote, local) =>
      // listener ! c
      val connection = sender()
      connection ! Register(self)
      context.become {
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
  }
}
