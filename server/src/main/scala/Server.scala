package neovim.scala
import akka.actor.{ActorRef, Actor, ActorSystem, ActorContext, Props, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config._
import scala.concurrent.duration._
import collection.JavaConversions._
import nvim.protocol._
import nvim._

object Server extends App {
  import system.dispatcher
  implicit val timeout = Timeout(25 seconds)

  val system = ActorSystem("system")

  val host = "127.0.0.1"
  val port = 8000

  val nv = Nvim(new Connection(host, port))
  nv.sendVimCommand("vsplit")
  nv.sendVimCommand("file [test]")
}
