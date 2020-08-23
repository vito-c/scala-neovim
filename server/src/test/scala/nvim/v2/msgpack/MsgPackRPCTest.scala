package nvim.v2.msgpack

import utest._
import com.rallyhealth.weepack.v1.Msg
import com.rallyhealth.weepickle.v1.WeePickle.FromScala
import com.rallyhealth.weepack.v1.Arr
import com.rallyhealth.weepack.v1.Str
import scala.collection.mutable.ArrayBuffer
import nvim.v2.Request
import com.rallyhealth.weepack.v1.FromMsgPack
import com.rallyhealth.weejson.v1.jackson.ToJson
import nvim.v2.{Command,Notification}

object MsgPackRPCTest extends TestSuite{
  val tests = Tests{
    test("Request"){
      val msg = new Arr(ArrayBuffer(Str("abc"), Str("def")))
      val rq = Request(1,2,"dothis", List(Str("abc"), Str("def")))
      val bin = FromScala(rq).transform(ToMsgPackRPC.bytes)
      val json = FromMsgPack(bin).transform(ToJson.string)
      println(json)
      assert(json == """[1,2,"dothis",["abc","def"]]""")
    }
    test("Notification"){
      val msg = new Arr(ArrayBuffer(Str("abc"), Str("def")))
      val rq = Notification(2,"dothis", List(Str("abc"), Str("def")))
      val bin = FromScala(rq).transform(ToMsgPackRPC.bytes)
      val json = FromMsgPack(bin).transform(ToJson.string)
      println(json)
      assert(json == """[2,"dothis",["abc","def"]]""")
    }
    test("Command") {
      val cm = Command("vsplit")
      val bin = FromScala(cm.notification).transform(ToMsgPackRPC.bytes)
      val json = FromMsgPack(bin).transform(ToJson.string)
      println(json)
      assert(json == """[0,"nvim_command",["vsplit"]]""")
    }
  }
}
