package scalapb_json

import com.google.protobuf.struct.{Struct, Value}
import scalapb.{GeneratedMessageCompanion, GeneratedMessage, Message}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object ProtoMacrosJava {
  implicit class ScalaPBMessageOps[A <: GeneratedMessage with Message[A]](private val companion: GeneratedMessageCompanion[A]) extends AnyVal {
    def fromTextFormat(textFormat: String): A =
      macro ProtoMacros.fromTextFormatImpl

    def fromTextFormatDebug(textFormat: String): A =
      macro ProtoMacros.fromTextFormatDebugImpl
  }
}

class ProtoMacrosJava(override val c: blackbox.Context) extends ProtoMacros(c) {

  import c.universe._

}
