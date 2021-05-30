package scalapb_json

import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

// TODO enable compile time check
object ProtoMacros {
  implicit class ScalaPBMessageOps[A <: GeneratedMessage](
    private val companion: GeneratedMessageCompanion[A]
  ) extends AnyVal {
    inline def fromTextFormat(textFormat: String): A =
      companion.fromAscii(textFormat)

    inline def fromTextFormatDebug(textFormat: String): A =
      companion.fromAscii(textFormat)
  }
}
