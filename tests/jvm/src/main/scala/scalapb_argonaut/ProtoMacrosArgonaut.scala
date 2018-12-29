package scalapb_argonaut

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

import scala.reflect.macros.blackbox
import language.experimental.macros
import scala.util.Try

object ProtoMacrosArgonaut {

  implicit class ProtoContext(private val c: StringContext) extends AnyVal {
    def struct(): com.google.protobuf.struct.Struct =
      macro ProtoMacrosArgonaut.protoStructInterpolation
    def value(): com.google.protobuf.struct.Value =
      macro ProtoMacrosArgonaut.protoValueInterpolation
  }

  implicit class FromJson[A <: GeneratedMessage with Message[A]](
    val companion: GeneratedMessageCompanion[A]
  ) extends AnyVal {
    def fromJsonConstant(json: String): A = macro ProtoMacrosArgonaut.fromJsonConstantImpl0[A]

    def fromJson(json: String): A = macro ProtoMacrosArgonaut.fromJsonImpl[A]

    def fromJsonDebug(json: String): A = macro ProtoMacrosArgonaut.fromJsonDebugImpl

    def fromJsonOpt(json: String): Option[A] =
      macro ProtoMacrosArgonaut.fromJsonOptImpl[A]

    def fromJsonEither(json: String): Either[Throwable, A] =
      macro ProtoMacrosArgonaut.fromJsonEitherImpl[A]

    def fromJsonTry(json: String): Try[A] =
      macro ProtoMacrosArgonaut.fromJsonTryImpl[A]
  }
}

class ProtoMacrosArgonaut(override val c: blackbox.Context)
  extends scalapb_json.ProtoMacrosCommon(c) {

  import c.universe._

  override def fromJsonImpl[A: c.WeakTypeTag](json: c.Tree): c.Tree = {
    val A = weakTypeTag[A]
    q"_root_.scalapb_argonaut.JsonFormat.fromJsonString[$A]($json)"
  }

  override def fromJsonConstantImpl[A <: GeneratedMessage with Message[A]: c.WeakTypeTag: GeneratedMessageCompanion](
    string: String
  ): c.Tree = {
    val A = weakTypeTag[A]
    scalapb_argonaut.JsonFormat.fromJsonString[A](string)
    q"_root_.scalapb_argonaut.JsonFormat.fromJsonString[$A]($string)"
  }

  private[this] def parseJson(json: String): argonaut.Json = {
    argonaut.JsonParser.parse(json).left.map(sys.error(_)).merge
  }

  override protected[this] def protoString2Struct(string: String): c.Tree = {
    val json = parseJson(string)
    val struct = StructFormat.structParser(json)
    q"$struct"
  }

  override protected[this] def protoString2Value(string: String): c.Tree = {
    val json = parseJson(string)
    val value = StructFormat.structValueParser(json)
    q"$value"
  }
}
