package nvim.v2.msgpack

import java.io.ByteArrayOutputStream

import com.rallyhealth.weepickle.v1.core.Visitor
import com.rallyhealth.weepack.v1.Msg
import com.rallyhealth.weepack.v1.MsgPackRenderer

object ToMsgPackRPC {

  /**
    * Write the given MessagePack struct as a binary
    */
  def bytes: Visitor[ByteArrayOutputStream, Array[Byte]] = {
    pprint.log("BYTES")
    outputStream(new ByteArrayOutputStream()).map(_.toByteArray)
  }

  /**
    * Write the given MessagePack struct as a binary to the given OutputStream
    */
  def outputStream[OutputStream <: java.io.OutputStream](
      out: OutputStream
  ): Visitor[OutputStream, OutputStream] = {
    pprint.log("OUTIE")
    new MsgPackRPC(out)
  }

  def ast: Visitor[Msg, Msg] = {
    pprint.log("AST")
    Msg
  }
}
