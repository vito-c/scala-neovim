package nvim.protocol

import msgpack4z._
import msgpack4z.CodecInstances.all._

final case class Notification(tpe: Int, method: String, params: MsgpackUnion) {
  override def toString = {
    val sb = new StringBuilder
    sb.append("Notification(\n")
    sb.append("  type: ").append(tpe).append(",\n")
    sb.append("  method: ").append(method).append(",\n")
    sb.append("  params: ").append(NvimHelper.msgpackUnionAsString(params, nest = 1)).append("\n")
    sb.append(")")
    sb.toString
  }
}

object Notification {
  implicit val instance: MsgpackCodec[Notification] = CaseCodec.codec(Notification.apply _, Notification.unapply _)
}

object NvimHelper {

  def msgpackUnionAsString(u: MsgpackUnion, nest: Int): String = {
    val sb = new StringBuilder

    def n(nest: Int) = "  "*nest

    def loop(u: MsgpackUnion, nest: Int): Unit = u match {
      case MsgpackArray(list) =>
        sb.append("[\n")
        list foreach { u =>
          sb.append(n(nest+1))
          loop(u, nest+1)
          sb.append(",\n")
        }
        sb.append(n(nest))
        sb.append("]")
      case MsgpackMap(map) =>
        sb.append("{\n")
        map foreach {
          case (k, v) =>
            sb.append(n(nest+1))
            loop(k, nest+1)
            sb.append(" -> ")
            loop(v, nest+1)
            sb.append(",\n")
        }
        sb.append(n(nest))
        sb.append("}")
      case MsgpackBinary(binary) =>
        sb.append(new String(binary, "UTF-8"))
      case MsgpackTrue =>
        sb.append("true")
      case MsgpackFalse =>
        sb.append("false")
      case MsgpackNil =>
        sb.append("nil")
      case MsgpackDouble(d) =>
        sb.append(s"double($d)")
      case MsgpackLong(l) =>
        sb.append(s"long($l)")
      case MsgpackULong(bi) =>
        sb.append(s"ulong($bi)")
      case MsgpackString(str) =>
        sb.append(s"str($str)")
      case MsgpackExt(exttype, data) =>
        sb.append(exttype).append(" : ").append(data.mkString("[", ", ", "]"))
    }

    loop(u, nest)
    sb.toString()
  }

  /**
   * Converts a `Array[Byte]` to a String. The bytes are decoded with UTF-8.
   */
  def asString(bin: Array[Byte]): String =
    new String(bin, "UTF-8")

  /**
   * Tries to parse some input `a` by `converter`. If `converter` is not defined
   * for the input, an [[nvim.UnexpectedResponse]] is thrown, otherwise the
   * result of `converter` is returned.
   */
  def parse[A, B](converter: PartialFunction[A, B])(a: A): B = {
    if (!converter.isDefinedAt(a))
      throw new UnexpectedResponse(s"Request can't handle `$a`.")
    else
      converter(a)
  }
}

