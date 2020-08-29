package nvim.v2

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.reflect.api._
import com.rallyhealth.weepack.v1._
import scala.collection.mutable.ArrayBuffer

@compileTimeOnly("enable scalacOptions -Ymacro-annotations")
class api extends StaticAnnotation {

  def macroTransform(
    annottees: Any*
  ): Any = macro api.impl
}

object api {

  def stub(
    pkg: String
  ): String = {
    val out = pkg.split("v2.").last.split("\\.[A-Z]").head
    out.split('.').take(2).mkString("_")
  }

  def snakeToCamel(
    str: String
  ): String = {
    str.split("_").map(s => s.take(1).toUpperCase ++ s.substring(1)).mkString
  }

  def camelToSnake(
    str: String
  ): String = {
    str.split("(?=[A-Z]+)").map(_.toLowerCase).mkString("_")
  }

  def impl(
    c: Context
  )(
    annottees: c.Expr[Any]*
  ): c.Expr[Any] = {

    import c.universe._

    def packparams(
      paramss: List[List[c.universe.ValDef]]
    ): List[Tree] = {
      def go(
        ls: List[c.universe.ValDef]
      ): List[Tree] = {
        pprint.log(ls)

        ls.collect {
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Float" =>
            q"com.rallyhealth.weepack.v1.Float32(${name})"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Double" =>
            q"com.rallyhealth.weepack.v1.Float64(${name})"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Int" =>
            q"com.rallyhealth.weepack.v1.Int32(${name})"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Long" =>
            q"com.rallyhealth.weepack.v1.Int64(${name})"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "String" =>
            pprint.log(vd,  "string")
            q"com.rallyhealth.weepack.v1.Str(${name})"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Boolean" =>
            q"com.rallyhealth.weepack.v1.Bool(${name})"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Arr" =>
            q"$name"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Obj" =>
            q"$name"
          case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Msg" =>
            q"$name"

          case x =>
            pprint.log(x)
            c.abort(
              c.enclosingPosition,
              "Oops that type for Msg is unsuported use a list or something"
            )
        }
      }
      go(paramss.flatten)
    }

    def nvimCommand(
      symbol: Symbol,
      tpname: TypeName
    ): String = {
      stub(symbol.fullName) + "_" + tpname.toString.toLowerCase
    }

    val result = {
      annottees.map(_.tree).toList match {
        case cd @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: Nil => {

          val fn = c.freshName(newTypeName(tpname.toString))
          val probe = c.typeCheck(q""" {class $fn; ()} """)
          val owner: Symbol = probe match { case Block(List(t), r) => t.symbol.owner }

          // case i: Arr => q"com.rallyhealth.we.v1.Arr(${i.value})"
          // implicit val liftArr = Liftable[Arr] { i =>
          //   q"com.rallyhealth.weepack.v1.Arr(${i.value})"
          // }
//
// object Arr {
//   def apply(items: Msg*): Arr = Arr(items.to(mutable.ArrayBuffer))
// }
// case class Obj(value: mutable.LinkedHashMap[Msg, Msg]) extends Msg
// object Obj {
//   def apply(item: (Msg, Msg), items: (Msg, Msg)*): Obj = {
//     val map = new mutable.LinkedHashMap[Msg, Msg]()
//     map.put(item._1, item._2)
//     for (i <- items) map.put(i._1, i._2)
//     Obj(map)
//   }
//
//   def apply(): Obj = Obj(new mutable.LinkedHashMap[Msg, Msg]())
// }
//

// Binary
// Float32
// Float64
// Int32
// Int64
// UInt64

          // implicit val liftInt32 = Liftable[Int32] { i =>
          //   q"com.rallyhealth.weepack.v1.Int32(${i.value})"
          // }
          // implicit val liftInt64 = Liftable[Int64] { i =>
          //   q"com.rallyhealth.weepack.v1.Int64(${i.value})"
          // }
          // implicit val liftUInt64 = Liftable[UInt64] { i =>
          //   q"com.rallyhealth.weepack.v1.UInt64(${i.value})"
          // }
          // implicit val liftFloat32 = Liftable[Float32] { i =>
          //   q"com.rallyhealth.weepack.v1.Float32(${i.value})"
          // }
          // implicit val liftFloat64 = Liftable[Float64] { i =>
          //   q"com.rallyhealth.weepack.v1.Float64(${i.value})"
          //
          // }
          // implicit val liftBinary = Liftable[Binary] { i =>
          //   q"com.rallyhealth.weepack.v1.Binary(${i.value})"
          // }
          // implicit val liftStr = Liftable[Str] { s =>
          //   q"com.rallyhealth.weepack.v1.Str(${s.value})"
          // }
          implicit def liftMsg: Liftable[Msg] = Liftable[Msg] { m =>
            def go(
              msg: Msg
            ): Tree = {
              msg match {
                case i: Binary => q"com.rallyhealth.we.v1.Binary(${i.value})"
                case i: Float32 => q"com.rallyhealth.we.v1.Float32(${i.value})"
                case i: Float64 => q"com.rallyhealth.we.v1.Float64(${i.value})"
                case i: Int32 => q"com.rallyhealth.we.v1.Int32(${i.value})"
                case i: Int64 => q"com.rallyhealth.we.v1.Int64(${i.value})"
                case i: UInt64 => q"com.rallyhealth.we.v1.UInt64(${i.value})"
                case s: Str => q"com.rallyhealth.weepack.v1.Str(${s.value})"
                // case a: Arr =>
                //   val inner = a.value.map(v => go(v))
                //   val code =  reify { """{Arr(Str("def"),Str("abc"))}""" }
                //   pprint.log(code)
                //   pprint.log("fart")
                //   q"com.rallyhealth.weepack.v1.Arr(..$inner)"
                case _ =>
                  c.abort(
                    c.enclosingPosition,
                    "Oops that Msg is unsuported"
                  )
              }
            }
            go(m)

          // m match {
          //   case i: Binary => q"com.rallyhealth.we.v1.Binary(${i.value})"
          //   case i: Float32 => q"com.rallyhealth.we.v1.Float32(${i.value})"
          //   case i: Float64 => q"com.rallyhealth.we.v1.Float64(${i.value})"
          //   case i: Int32 => q"com.rallyhealth.we.v1.Int32(${i.value})"
          //   case i: Int64 => q"com.rallyhealth.we.v1.Int64(${i.value})"
          //   case i: UInt64 => q"com.rallyhealth.we.v1.UInt64(${i.value})"
          //   case s: Str => q"com.rallyhealth.weepack.v1.Str(${s.value})"
          //   case a: Arr =>
          //     q"com.rallyhealth.we.v1.Arr(..${a.value})"
          //   // case o: Obj => q"com.rallyhealth.we.v1.Obj(${Obj(o.value)})"
          //   case _ =>
          //     c.abort(
          //       c.enclosingPosition,
          //       "Oops that Msg is unsuported"
          //     )
          // }
          }
          q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents {
            $self => ..$stats
            def notice = nvim.v2.Notification(2, ${nvimCommand(owner, tpname)}, ${packparams(
            paramss
          )})
            def request(code:Int) = Request(0, code, ${nvimCommand(owner, tpname)}, List())
          }"""
        }
        case x =>
          pprint.log(x)
          c.abort(
            c.enclosingPosition,
            "Oops something went wrong put this annotation on a case class"
          )
      }
    }
    c.Expr[Any](result)

  }
}
