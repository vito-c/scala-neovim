// package nvim.v1
//
// import scala.concurrent.ExecutionContext
// import scala.concurrent.Future
//
// import msgpack4z._
// import msgpack4z.MsgpackUnion._
// import nvim.v1.protocol.NvimHelper
// import nvim.v1.protocol.UnexpectedResponse
//
// /**
//  * Low level wrapper around Nvims msgpack-rpc protocol. Represents a Nvim
//  * buffer; all operations operate directly on the buffer data structure of Nvim.
//  */
// final case class Buffer(id: Int, connection: Connection) {
//
//   /**
//    * Returns the absolute path of the file that is represented by this buffer.
//    */
//   def name(implicit ec: ExecutionContext): Future[String] = {
//     connection.sendRequest("buffer_get_name", int(id)) {
//       case MsgpackBinary(bin) ⇒ NvimHelper.asString(bin)
//     }
//   }
//
//   /**
//    * The number of lines. The line count for empty buffers is 1.
//    */
//   def lineCount(implicit ec: ExecutionContext): Future[Int] = {
//     connection.sendRequest("buffer_line_count", int(id)) {
//       case MsgpackLong(long) ⇒ long.toInt
//     }
//   }
//
//   /**
//    * Returns the line at `index`, where line 1 is represented by index 0. If a
//    * large range of files should be retrieved, it is more efficient to use
//    * [[lineSlice]] instead.
//    *
//    * @example {{{
//    * val fileContent = for {
//    *   count ← buffer.lineCount
//    *   lines ← Future.sequence(0 to count map buffer.lineAt)
//    * } yield lines.mkString("\n")
//    * }}}
//    */
//   def lineAt(index: Int)(implicit ec: ExecutionContext): Future[String] = {
//     connection.sendRequest("buffer_get_line", int(id), int(index)) {
//       case MsgpackBinary(bin) => NvimHelper.asString(bin)
//     }
//   }
//
//   /**
//    * Returns all the lines between `start` and `end`. With `includeStart` and
//    * `includeEnd` it can be chosen whether `start` and `end` should be inclusive
//    * or exclusive. The default parameters are set to inclusive for `start` and
//    * exclusive for `end`.
//    *
//    * @example {{{
//    * buffer.lineSlice(7, 10)
//    * // this may return
//    * // Seq("line7", "line8", "line9")
//    * }}}
//    */
//   def lineSlice(
//     start: Int,
//     end: Int,
//     includeStart: Boolean = true,
//     includeEnd: Boolean = false
//   )(implicit ec: ExecutionContext): Future[Seq[String]] = {
//     connection.sendRequest("buffer_get_line_slice", int(id), int(start), int(end), bool(includeStart), bool(includeEnd)) {
//       case MsgpackArray(xs) => xs map NvimHelper.parse {
//         case MsgpackString(str) => str
//       }
//     }
//   }
//
//   /**
//    * Returns the position of the mark that is associated with `name`.
//    * @example {{{
//    * val lastChangePosition = buffer.mark('.')
//    * // this returns the position where the last change occurred in current buffer
//    * }}}
//    */
//   def mark(name: Char)(implicit ec: ExecutionContext): Future[Position] = {
//     connection.sendRequest("buffer_get_mark", int(id), string(name.toString)) {
//       case MsgpackArray(List(MsgpackLong(row), MsgpackLong(col))) ⇒
//         Position(row.toInt, col.toInt)
//     }
//   }
//
//   /**
//    * Returns the content of `line`. The first line is indexed with 0.
//    */
//   def line(line: Int)(implicit ec: ExecutionContext): Future[String] = {
//     connection.sendRequest("buffer_get_line", int(id), int(line)) {
//       case MsgpackBinary(bin) ⇒ NvimHelper.asString(bin)
//     }
//   }
//
// }
