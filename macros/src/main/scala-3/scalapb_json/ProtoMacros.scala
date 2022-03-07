package scalapb_json

import scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import scala.quoted.*

object ProtoMacros {
  implicit class ScalaPBMessageOps[A <: GeneratedMessage](
    private val companion: GeneratedMessageCompanion[A]
  ) extends AnyVal {
    inline def fromTextFormat(textFormat: String): A =
      ${ fromTextFormatImpl('textFormat, 'companion, debug = false) }

    inline def fromTextFormatDebug(textFormat: String): A =
      ${ fromTextFormatImpl('textFormat, 'companion, debug = true) }
  }

  private def fromTextFormatImpl[A <: GeneratedMessage: Type](
    textFormat: Expr[String],
    companion: Expr[GeneratedMessageCompanion[A]],
    debug: Boolean
  )(using quote: Quotes): Expr[A] = {
    import quote.reflect.*
    textFormat.value match {
      case Some(str) =>
        val typeName = Type.show[A]
        val javaClass = Class.forName(typeName + "$").getMethods.filter { method =>
          // https://github.com/scalapb/ScalaPB/blob/v0.11.9/scalapb-runtime/src/main/scala/scalapb/GeneratedMessageCompanion.scala#L161
          method.getName == "toJavaProto" && method.getReturnType != classOf[Object]
        } match {
          case Array(method) =>
            method.getReturnType
          case other =>
            report.errorAndAbort(s"Could not found Java class ${typeName} ${other.mkString(" ")}")
        }
        val builder = javaClass
          .getMethod("newBuilder")
          .invoke(null)
          .asInstanceOf[com.google.protobuf.Message.Builder]
        com.google.protobuf.TextFormat.getParser.merge(str, builder)
        val result = builder.build().toString
        if (debug) {
          report.info(result)
        }
        '{ ${ companion }.fromAscii(${ textFormat }) }
      case None =>
        report.errorAndAbort("expect String literal")
    }
  }

}
