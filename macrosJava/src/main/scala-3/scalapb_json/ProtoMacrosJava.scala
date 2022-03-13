package scalapb_json

import com.google.protobuf.util.JsonFormat
import scalapb.JavaProtoSupport
import scalapb_json.ProtoMacros.getJavaClass
import com.google.protobuf.Message.Builder
import scala.quoted.{Expr, Quotes, Type}
import scala.reflect.ClassTag

object ProtoMacrosJava {
  implicit class ScalaBPMessageOps[A, B](
    private val companion: JavaProtoSupport[A, B]
  ) extends AnyVal {
    inline def fromJsonConstant(json: String): A =
      ${ fromJsonConstantImpl('json, 'companion) }

    def fromJson(json: String)(implicit c: ClassTag[B]): A = {
      val parser = JsonFormat.parser()
      val builder = c.runtimeClass.getMethod("newBuilder").invoke(null).asInstanceOf[Builder]
      parser.merge(json, builder)
      val result = builder.build().asInstanceOf[B]
      companion.fromJavaProto(result)
    }
  }

  private[this] def validate(json: String, clazz: Class[?]): Unit = {
    val parser = JsonFormat.parser()
    val builder =
      clazz.getMethod("newBuilder").invoke(null).asInstanceOf[Builder]
    parser.merge(json, builder)
    builder.build()
  }

  private def fromJsonConstantImpl[A: Type, B: Type](
    json: Expr[String],
    companion: Expr[JavaProtoSupport[A, B]]
  )(using quote: Quotes): Expr[A] = {
    import quote.reflect.*
    json.value match {
      case Some(str) =>
        val typeName = Type.show[A]
        val javaClass = getJavaClass(typeName).left.map(report.errorAndAbort(_)).merge
        validate(str, javaClass)
        '{ ${ companion }.fromJson($json)(ClassTag[B](${ Expr(javaClass) })) }
      case None =>
        report.errorAndAbort("expect String literal")
    }
  }

}
