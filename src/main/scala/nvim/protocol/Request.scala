package nvim.protocol

import msgpack4z._
import msgpack4z.CodecInstances.all._

final case class Request(tpe: Int, id: Int, name: String, params: MsgpackUnion = MsgpackUnion.array(List())) {
  override def toString = {
    val sb = new StringBuilder
    sb.append("Request(\n")
    sb.append("  type: ").append(tpe).append(",\n")
    sb.append("  id: ").append(id).append(",\n")
    sb.append("  name: ").append(name).append(",\n")
    sb.append("  params: ").append(NvimHelper.msgpackUnionAsString(params, nest = 1)).append("\n")
    sb.append(")")
    sb.toString
  }
}

object Request {
  implicit val instance: MsgpackCodec[Request] = CaseCodec.codec(Request.apply _, Request.unapply _)
}
