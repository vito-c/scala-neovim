import akka.actor.{ActorRef, Actor, ActorSystem, ActorContext, Props, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
object Main extends App {
  import system.dispatcher
  implicit val timeout = Timeout(25 seconds)

  val system   = ActorSystem("system")
  val xactor = system.actorOf(
    Props(classOf[XMPPActor], conn),
    name = "xmpp"
  )

  val conf = ConfigFactory.load("v2d2.conf")
  val rooms = conf.getList("v2d2.rooms").toList

  rooms foreach { entry =>
    val config = (entry.asInstanceOf[ConfigObject]).toConfig();
    val name = config.getString("name")
    val pass = config.getString("pass")
    xactor ! JoinRoom(name, Some(pass))
    println(s"name: ${name} pass: ${pass}") 
  }

  val actors = conf.getList("v2d2.actors").toList
  actors foreach { entry =>
    println(s"entry: ${entry}")
  }

}
