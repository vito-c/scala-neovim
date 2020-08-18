package nvim.v1

import java.net.Socket
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.rallyhealth.weepack.v1._
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker

// import com.typesafe.scalalogging.LazyLogging

import msgpack4z._
import nvim.protocol._
import com.rallyhealth.weepickle.v1.WeePickle.ToScala
import org.slf4j.LoggerFactory

/**
 * Manages the connection to Neovim. Communication is done through the
 * msgpack-rpc protocol, whose spec is here:
 *
 * [[https://github.com/msgpack-rpc/msgpack-rpc/blob/master/spec.md]]
 */
final class Connection(host: String, port: Int) {
  val logger = LoggerFactory.getLogger(getClass)

  val ResponseId = 1
  val NotificationId = 2

  private val requests = TrieMap[Int, Response => Unit]()
  private val notificationHandlers: mutable.Set[Notification => Unit] = {
    import scala.collection.JavaConverters._
    new ConcurrentSkipListSet().asScala
  }

  private val gen = new IdGenerator
  private val socket = new Socket(host, port)
  private val thread = new Thread(new Runnable {
    override def run() = {

      // val munp = new InputStreamMsgPackParser(socket.getInputStream,128,256)
      
      val unp = MessagePack.DEFAULT.newUnpacker(socket.getInputStream)
      val unpacker = new Msgpack07Unpacker(unp)
      readResp(unp, unpacker)

      logger.info(s"Connection to $host:$port lost")
    }
  })
  thread.start()

  private def readResp(unp: MessageUnpacker, unpacker: Msgpack07Unpacker): Unit = {
    /*
     * The stupid API doesn't allow to do a look ahead, therefore we have to
     * use reflection to correct the internal position in the buffer.
     */
    def lookAheadType(): Int = {
      unpacker.unpackArrayHeader()
      val _type = unpacker.unpackInt()
      val _field = classOf[MessageUnpacker].getDeclaredField("position")
      _field.setAccessible(true)
      val pos = _field.getInt(unp)
      _field.setInt(unp, pos-2)
      _type
    }

    lookAheadType() match {
      case ResponseId =>
        MsgpackCodec[Response].unpack(unpacker) match {
          case scalaz.-\/(e) =>
            logger.error("Couldn't unpack response", e)

          case scalaz.\/-(resp) =>
            logger.debug(s"retrieved: $resp")
            requests.remove(resp.id) match {
              case Some(_field) =>
                _field(resp)
              case None =>
                logger.warn(s"The following response is ignored because its ID '${resp.id}' is unexpected: $resp")
            }
        }

      case NotificationId =>
        MsgpackCodec[Notification].unpack(unpacker) match {
          case scalaz.-\/(e) =>
            logger.error("Couldn't unpack response", e)

          case scalaz.\/-(notification) =>
            logger.debug(s"retrieved: $notification")
            notificationHandlers foreach (_(notification))
        }

      case _type =>
        logger.error(s"Unknow type: ${_type}", new Throwable)
    }

    if (unp.hasNext())
      readResp(unp, unpacker)
  }

  /**
   * Adds a new notification handler and notifies it for every incoming
   * notification event. If the handler is already added, it is not added again.
   */
  def addNotificationHandler(handler: Notification => Unit): Unit = {
    notificationHandlers += handler
  }

  /**
   * Removes the notification handler from the list of registered handlers.
   */
  def removeNotificationHandler(handler: Notification => Unit): Unit = {
    notificationHandlers -= handler
  }

  /**
   * Sends a [[nvim.internal.Notification]] to Nvim.
   */
  def sendNotification[A](
    method: String,
    params: MsgpackUnion*
  ) : Unit = {
    val ps = MsgpackUnion.array(params.toList)
    val req = Notification(2, method, ps)

    val bytes = MsgpackCodec[Notification].toBytes(req, new Msgpack07Packer)
    val strs = (bytes.map(_.toChar)).mkString 
    logger.debug(s"sending: $req")
    logger.debug(s"raw: $strs")
    val out = socket.getOutputStream
    out.write(bytes)
    out.flush()
  }

  /**
   * Sends a [[nvim.internal.Request]] to Nvim and returns the value that is in
   * the response. `converter` is used to convert the data that is sent over the
   * wire into the actual value that is expected.
   */
  def sendRequest[A](
    command: String,
    params: MsgpackUnion*
  ) (converter: PartialFunction[MsgpackUnion, A] ) : Future[A] = {
    val id = gen.nextId()
    val ps = MsgpackUnion.array(params.toList)
    val req = Request(0, id, command, ps)

    val promise = Promise[A]
    val _field: Response => Unit = { resp =>
      if (!converter.isDefinedAt(resp.result))
        promise.failure(new UnexpectedResponse(resp.result.toString))
      else
        Try(converter(resp.result)) match {
          case Success(value) => promise.success(value)
          case Failure(f) => promise.failure(f)
        }
    }

    val bytes = MsgpackCodec[Request].toBytes(req, new Msgpack07Packer)
    requests += req.id -> _field
    logger.debug(s"sending: $req")
    val out = socket.getOutputStream
    out.write(bytes)
    out.flush()

    promise.future
  }

  def close(): Unit =
    socket.close()
}

private final class IdGenerator {

  private val id = new AtomicInteger(0)

  def nextId(): Int = id.getAndIncrement
}

final class InvalidResponse(msg: String) extends RuntimeException(msg)
