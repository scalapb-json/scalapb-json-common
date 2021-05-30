package scalapb_json

import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object ProtoMacros {
  implicit class ScalaPBMessageOps[A <: GeneratedMessage](
    private val companion: GeneratedMessageCompanion[A]
  ) extends AnyVal {
    def fromTextFormat(textFormat: String): A =
      macro ProtoMacros.fromTextFormatImpl

    def fromTextFormatDebug(textFormat: String): A =
      macro ProtoMacros.fromTextFormatDebugImpl
  }

  def getJavaType(c: blackbox.Context)(tree: c.Tree): Class[_] = {
    val fullName = tree.tpe.companion.typeSymbol.fullName
    Class.forName(fullName + "$").getMethods.filter { method =>
      // https://github.com/scalapb/ScalaPB/blob/v0.8.2/scalapb-runtime/shared/src/main/scala/scalapb/GeneratedMessageCompanion.scala#L189
      method.getName == "toJavaProto" && method.getReturnType != classOf[Object]
    } match {
      case Array(method) =>
        method.getReturnType
      case other =>
        c.abort(
          c.enclosingPosition,
          s"Could not found Java class ${fullName} ${other.mkString(" ")}"
        )
    }
  }
}

final class ProtoMacros(val c: blackbox.Context) {
  import c.universe._

  def fromTextFormatDebugImpl(textFormat: c.Tree): c.Tree = {
    val code = fromTextFormatImpl0(textFormat, debug = true)
    println(showCode(code))
    code
  }

  def fromTextFormatImpl(textFormat: c.Tree): c.Tree = {
    fromTextFormatImpl0(textFormat, debug = false)
  }

  private[this] def fromTextFormatImpl0(textFormat: c.Tree, debug: Boolean): c.Tree = {
    c.prefix.tree match {
      case q"$_($lhs)" =>
        val javaType = ProtoMacros.getJavaType(c)(lhs)
        val Literal(Constant(str: String)) = textFormat
        val builder = javaType
          .getMethod("newBuilder")
          .invoke(null)
          .asInstanceOf[com.google.protobuf.Message.Builder]
        com.google.protobuf.TextFormat.getParser.merge(str, builder)
        val result = builder.build()
        if (debug) {
          println(result)
        }

        q"""${lhs}.fromAscii($textFormat)"""
    }
  }
}
