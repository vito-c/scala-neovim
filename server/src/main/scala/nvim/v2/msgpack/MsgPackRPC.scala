package nvim.v2.msgpack

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.format.DateTimeFormatter

import com.rallyhealth.weepack.v1.Msg.visitString
import com.rallyhealth.weepack.v1.{MsgPackKeys => MPK}
import com.rallyhealth.weepickle.v1.core.{ArrVisitor, ObjVisitor, Visitor}
import com.rallyhealth.weepack.v1.MsgVisitor
import com.rallyhealth.weepack.v1.MsgPackRenderer
import com.rallyhealth.weepickle.v1.core.NoOpVisitor

class MsgPackRPC[T <: java.io.OutputStream](
    out: T = new ByteArrayOutputStream()
) extends MsgPackRenderer[T](out) {
  override def visitObject(length: Int): ObjVisitor[T, T] =
    new ObjVisitor[T, T] {
      require(
        length != -1,
        "Length of com.rallyhealth.weepack.v1 object must be known up front"
      )
      if (length <= 15) {
        out.write(MPK.FixArrMask | length)
      } else if (length <= 65535) {
        out.write(MPK.Map16)
        writeUInt16(length)
      } else {
        out.write(MPK.Map32)
        writeUInt32(length)
      }

      def subVisitor: Visitor[_, _] = {
        MsgPackRPC.this
      }

      def visitKey(): Visitor[_, _] = {
        NoOpVisitor
      }
      def visitKeyValue(s: Any): Unit = () // do nothing
      def visitValue(v: T): Unit = () // do nothing

      def visitEnd(): T = out // do nothing
    }
}
