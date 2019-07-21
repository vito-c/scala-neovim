package nvim.protocol

import msgpack4z._
import msgpack4z.CodecInstances.all._

final case class Response(tpe: Int, id: Int, error: MsgpackUnion, result: MsgpackUnion) {
  override def toString = {
    val sb = new StringBuilder
    sb.append("Response(\n")
    sb.append("  type: ").append(tpe).append(",\n")
    sb.append("  id: ").append(id).append(",\n")
    sb.append("  error: ").append(NvimHelper.msgpackUnionAsString(error, nest = 1)).append(",\n")
    sb.append("  result: ").append(NvimHelper.msgpackUnionAsString(result, nest = 1)).append("\n")
    sb.append(")")
    sb.toString
  }
}

object Response {
  implicit val instance: MsgpackCodec[Response] = CaseCodec.codec(Response.apply _, Response.unapply _)
}


