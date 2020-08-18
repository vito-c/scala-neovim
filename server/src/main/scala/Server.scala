package neovim.scala

import akka.actor.{ActorRef, Actor, ActorSystem, ActorContext, Props, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config._
import scala.concurrent.duration._
import collection.JavaConversions._
import nvim.protocol._
import nvim.v2._
import org.slf4j.{LoggerFactory}
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.net.InetSocketAddress
import java.net.InetAddress
import java.net.Inet4Address

object Server extends App {
  import system.dispatcher
  implicit val timeout = Timeout(25 seconds)
  val root:Logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
  root.setLevel(Level.ALL);

  val system = ActorSystem("system")

  val host = "127.0.0.1"
  val port = 8000
  system.log.info("fart")
  val logger = LoggerFactory.getLogger(getClass) 
  logger.info("hello")
  val neovim = system.actorOf(Props(new Nvim(new InetSocketAddress(host, port))), name = "neovim")
  neovim ! "hello"

  // val nv = Nvim(new Connection(host, port))
  // nv.sendVimCommand("vsplit")
}
