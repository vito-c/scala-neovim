import msgpack4z._
import msgpack4z.CodecInstances.all._
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker
import nvim.protocol._
import scala.collection.concurrent.TrieMap

import scala.collection.mutable
import java.net.Socket
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger

import nvim._

object Example {

  def main(args : Array[String]) { 
    val host = "127.0.0.1"
    val port = 8000

    val nv = Nvim( new Connection( host, port) )
    nv.sendVimCommand("vsplit")

  }

}
 

