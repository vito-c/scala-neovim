package nvim.v2.msgpack

import utest._
import com.rallyhealth.weepack.v1.Msg
import com.rallyhealth.weepickle.v1.WeePickle.FromScala
import com.rallyhealth.weepack.v1.ToMsgPack
import com.rallyhealth.weepack.v1.Arr
import com.rallyhealth.weepack.v1.Str
import scala.collection.mutable.ArrayBuffer
import nvim.v2.Request

object MsgPackRPC extends TestSuite{
  val tests = Tests{
    test("Request"){
      val msg = new Arr(ArrayBuffer(Str("abc"), Str("def")))
      val rq = Request(1,2,"method", List(msg))
      val ast = FromScala(rq).transform(ToMsgPackRPC
      pprint.log(ast)

    }
    // test("Response"){
    //   1
    // }
    // test("Notification"){
    //   val a = List[Byte](1, 2)
    //   a(10)
    // }
  }
}
