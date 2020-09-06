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
  ): Any = macro api.impl_api
}

@compileTimeOnly("enable scalacOptions -Ymacro-annotations")
class ui extends StaticAnnotation {

  def macroTransform(
    annottees: Any*
  ): Any = macro api.impl_ui
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

  def packparams(
    c: Context
  )(
    paramss: List[List[c.universe.ValDef]]
  ): List[c.universe.Tree] = {
    import c.universe._
    paramss.flatten.collect {
      case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Float" =>
        q"com.rallyhealth.weepack.v1.Float32(${name})"
      case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Double" =>
        q"com.rallyhealth.weepack.v1.Float64(${name})"
      case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Int" =>
        q"com.rallyhealth.weepack.v1.Int32(${name})"
      case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "Long" =>
        q"com.rallyhealth.weepack.v1.Int64(${name})"
      case vd @ ValDef(_, name, Ident(tpname), _) if tpname.toString == "String" =>
        pprint.log(vd, "string")
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

  implicit def liftMsg(
    c: Context
  ): c.universe.Liftable[Msg] = c.universe.Liftable[Msg] { m =>
    def go(
      msg: Msg
    ): c.universe.Tree = {
      import c.universe._
      msg match {
        case i: Binary => q"com.rallyhealth.we.v1.Binary(${i.value})"
        case i: Float32 => q"com.rallyhealth.we.v1.Float32(${i.value})"
        case i: Float64 => q"com.rallyhealth.we.v1.Float64(${i.value})"
        case i: Int32 => q"com.rallyhealth.we.v1.Int32(${i.value})"
        case i: Int64 => q"com.rallyhealth.we.v1.Int64(${i.value})"
        case i: UInt64 => q"com.rallyhealth.we.v1.UInt64(${i.value})"
        case s: Str => q"com.rallyhealth.weepack.v1.Str(${s.value})"
        case _ =>
          c.abort(
            c.enclosingPosition,
            "Oops that Msg is unsuported"
          )
      }
    }
    go(m)
  }

  def nvimCommand(
    c: Context
  )(
    symbol: c.universe.Symbol,
    tpname: c.universe.TypeName
  ): String = {
    "nvim_" + stub(symbol.fullName) + "_" + camelToSnake(tpname.toString)
  }

  def impl_api(
    c: Context
  )(
    annottees: c.Expr[Any]*
  ): c.Expr[Any] = {
    impl(c)(annottees)(false)
  }

  def impl_ui(
    c: Context
  )(
    annottees: c.Expr[Any]*
  ): c.Expr[Any] = {
    impl(c)(annottees)(true)
  }

  def impl(
    c: Context
  )(
    annottees: Seq[c.Expr[Any]]
  )(
    isUi: Boolean = false
  ): c.Expr[Any] = {
    import c.universe._

    val result = {
      annottees.map(_.tree).toList match {
        case cd @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: Nil => {

          val fn = c.freshName(newTypeName(tpname.toString))
          val probe = c.typeCheck(q""" {class $fn; ()} """)
          val owner: Symbol = probe match { case Block(List(t), r) => t.symbol.owner }

          val method = if (isUi) camelToSnake(tpname.toString) else nvimCommand(c)(owner, tpname)
          q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents {
            $self => ..$stats
            def notice = nvim.v2.Notification(2, ${method}, ${packparams(c)(
            paramss
          )})
            def request(code:Int) = Request(0, code, ${method}, List())
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
