package scalapb_json

import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.util.JsonFormat
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.Try

object ProtoMacrosJava {
  implicit class ProtoContext(private val c: StringContext) extends AnyVal {
    def struct(): com.google.protobuf.struct.Struct =
      macro ProtoMacrosJava.protoStructInterpolation

    def value(): com.google.protobuf.struct.Value =
      macro ProtoMacrosJava.protoValueInterpolation
  }

  implicit class ScalaBPMessageOps[A <: GeneratedMessage with Message[A]](
    val companion: GeneratedMessageCompanion[A]
  ) extends AnyVal {
    def fromJson(json: String): A = macro ProtoMacrosJava.fromJsonImpl0[A]

    def fromJsonDebug(json: String): A = macro ProtoMacrosJava.fromJsonDebugImpl

    def fromJsonOpt(json: String): Option[A] =
      macro ProtoMacrosJava.fromJsonOptImpl

    def fromJsonEither(json: String): Either[InvalidProtocolBufferException, A] =
      macro ProtoMacrosJava.fromJsonEitherImpl

    def fromJsonTry(json: String): Try[A] =
      macro ProtoMacrosJava.fromJsonTryImpl
  }
}

class ProtoMacrosJava(override val c: blackbox.Context) extends ProtoMacrosCommon(c) {
  import c.universe._

  private[this] def validate(json: String, clazz: Class[_]): Unit = {
    val parser = JsonFormat.parser()
    val builder =
      clazz.getMethod("newBuilder").invoke(null).asInstanceOf[com.google.protobuf.Message.Builder]
    parser.merge(json, builder)
    builder.build()
  }

  def fromJsonImpl[A <: GeneratedMessage with Message[A]: c.WeakTypeTag: GeneratedMessageCompanion](
    string: String
  ) = {
    val q"$_($lhs)" = c.prefix.tree
    val javaType = ProtoMacros.getJavaType(c)(lhs)
    validate(string, javaType)
    val packageAndOuter = javaType.getCanonicalName.split('.').init
    val p = packageAndOuter.map(TermName(_)).foldLeft(Ident(TermName("_root_")): Tree)(Select(_, _))
    val x = Select(p, TermName(javaType.getSimpleName))
    val builder, parser = TermName(c.freshName)
    q"""
      val $parser = _root_.com.google.protobuf.util.JsonFormat.parser()
      val $builder = $x.newBuilder()
      $parser.merge($string, $builder)
      $lhs.fromJavaProto($builder.build())
    """
  }

  override def protoString2Value(string: String): c.Tree = {
    val builder = com.google.protobuf.Value.newBuilder()
    JsonFormat.parser().merge(string, builder)
    val x = com.google.protobuf.struct.Value.fromJavaProto(builder.build())
    q"$x"
  }

  override def protoString2Struct(string: String): c.Tree = {
    val builder = com.google.protobuf.Struct.newBuilder()
    JsonFormat.parser().merge(string, builder)
    val x = com.google.protobuf.struct.Struct.fromJavaProto(builder.build())
    q"$x"
  }

}
