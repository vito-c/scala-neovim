package nvim.v2.msgpack

import utest._
import com.rallyhealth.weepack.v1.Msg
import com.rallyhealth.weepickle.v1.WeePickle.FromScala
import com.rallyhealth.weepack.v1.Arr
import com.rallyhealth.weepack.v1.Str
import com.rallyhealth.weepack.v1.Obj
import scala.collection.mutable.ArrayBuffer
import nvim.v2._
import com.rallyhealth.weepack.v1.FromMsgPack
import com.rallyhealth.weejson.v1.jackson.ToJson
import nvim.v2.{Command, Notification}
import com.rallyhealth.weepickle.v1.WeePickle.ToScala
import com.rallyhealth.weepickle.v1.WeePickle._
import nvim.v2.msgpack._
import com.rallyhealth.weepack.v1.Int32
import com.rallyhealth.weejson.v1.jackson.FromJson

object MsgPackRPCTest extends TestSuite {

  val tests = Tests {
    test("Request") {
      val msg = new Arr(ArrayBuffer(Str("abc"), Str("def")))
      val rq = Request(1, 2, "dothis", List(Str("abc"), Str("def")))
      val bin = FromScala(rq).transform(ToMsgPackRPC.bytes)
      val json = FromMsgPack(bin).transform(ToJson.string)
      println(json)
      assert(json == """[1,2,"dothis",["abc","def"]]""")
    }
    test("Notification") {
      val msg = new Arr(ArrayBuffer(Str("abc"), Str("def")))
      val rq = Notification(2, "dothis", List(Str("abc"), Str("def")))
      val bin = FromScala(rq).transform(ToMsgPackRPC.bytes)
      val json = FromMsgPack(bin).transform(ToJson.string)
      println(json)
      assert(json == """[2,"dothis",["abc","def"]]""")
    }
    test("Command") {
      val cm = Command("vsplit")
      // implicit val fmt: FromTo[nvim.v2.Command with nvim.v2.Notif] = macroFromTo[nvim.v2.Command with nvim.v2.Notif]

      val bin = FromScala(cm.notice).transform(ToMsgPackRPC.bytes)
      val json = FromMsgPack(bin).transform(ToJson.string)
      println(json)
      assert(json == """[2,"nvim_command",["vsplit"]]""")
    }

    test("macros") {
      assert(api.stub("nvim.v2.nvim.buf.Buffer") == "nvim_buf")
      assert(api.stub("nvim.v2.nvim.Nvim") == "nvim")
      assert(api.stub("a.v2.b.c.d.MyClass") == "b_c")
      assert(api.stub("a.v2.b.c.d") == "b_c")
      assert(api.stub("a.v2.b.c.MyClass") == "b_c")
      assert(api.stub("a.v2.b.c") == "b_c")
      assert(api.stub("a.v2.b") == "b")
      assert(api.stub("a.v2.b.MyClass") == "b")

      assert(api.snakeToCamel("abc_def_ghi_jkl") == "AbcDefGhiJkl")
      assert(api.camelToSnake("AbcDefGhiJkl") == "abc_def_ghi_jkl")

      // I will not support this as there is no need
      assert(api.camelToSnake("ABcDEfGhiJkl") != "aBc_dEf_ghi_jkl")
      assert(api.camelToSnake("ABcDEfGhiJkl") == "a_bc_d_ef_ghi_jkl")
    }
    test("macros list") {
      @nvim.v2.api
      case class Boo(
        foo: Arr,
        foo2: String
      ) {
        def test = "example"
      }
      val b = Boo(foo = Arr(Str("def"), Str("abc")), foo2 = "fooboo")
      assert(b.notice.params == List(Arr(Str("def"), Str("abc")), Str("fooboo")))
      @nvim.v2.api
      case class Coo(
        foo: Obj,
        foo2: String
      ) {
        def test = "example"
      }
      val c = Coo(foo = Obj((Str("key"), Int32(1))), foo2 = "fooboo")
      assert(c.notice.params == List(Obj((Str("key"), Int32(1))), Str("fooboo")))
    }

    test("ui macros") {

      @nvim.v2.ui
      case class FooBarBaz(
        foo: String
      ) {
        def test = "example"
      }
      val f = FooBarBaz("abc")

      assert(f.foo == "abc")
      assert(f.notice.method == "foo_bar_baz")
      assert(f.notice.params == List(Str("abc")))
      assert(f.test == "example")
    }

    test("api macros") {
      @nvim.v2.api
      case class LineCount(
        foo: String
      ) {
        def test = "example"
      }
      val l = LineCount("abc")
      pprint.log(l.notice.method)
      assert(l.notice.method == "nvim_msgpack_line_count")

      @nvim.v2.api
      case class Foo(
        foo: String
      ) {
        def test = "example"
      }
      val f = Foo("abc")

      pprint.log(f.notice.method)
      assert(f.foo == "abc")
      assert(f.notice.method == "nvim_msgpack_foo")
      assert(f.notice.params == List(Str("abc")))
      assert(f.test == "example")
    }
  }
}
