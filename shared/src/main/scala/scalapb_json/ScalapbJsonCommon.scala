package scalapb_json

import com.google.protobuf.ByteString
import scalapb._
import scalapb.descriptors._

object ScalapbJsonCommon {

  def unsignedInt(n: Int): Long = n & 0x00000000FFFFFFFFL

  type GenericCompanion = GeneratedMessageCompanion[T] forSome {
    type T <: GeneratedMessage with Message[T]
  }

  private[this] val PIntDefault = PInt(0)
  private[this] val PLongDefault = PLong(0L)
  private[this] val PFloatDefault = PFloat(0)
  private[this] val PDoubleDefault = PDouble(0)
  private[this] val PBooleanDefault = PBoolean(false)
  private[this] val PStringDefault = PString("")
  private[this] val PByteStringDefault = PByteString(ByteString.EMPTY)

  def defaultValue(fd: FieldDescriptor): PValue = {
    require(fd.isOptional)
    fd.scalaType match {
      case ScalaType.Int => PIntDefault
      case ScalaType.Long => PLongDefault
      case ScalaType.Float => PFloatDefault
      case ScalaType.Double => PDoubleDefault
      case ScalaType.Boolean => PBooleanDefault
      case ScalaType.String => PStringDefault
      case ScalaType.ByteString => PByteStringDefault
      case ScalaType.Enum(ed) => PEnum(ed.values(0))
      case ScalaType.Message(_) => throw new RuntimeException("No default value for message")
    }
  }

  def parseBigDecimal(value: String): BigDecimal = {
    try {
      // JSON doesn't distinguish between integer values and floating point values so "1" and
      // "1.000" are treated as equal in JSON. For this reason we accept floating point values for
      // integer fields as well as long as it actually is an integer (i.e., round(value) == value).
      BigDecimal(value)
    } catch {
      case e: Exception =>
        throw JsonFormatException(s"Not a numeric value: $value", e)
    }
  }

  def parseInt32(value: String): PValue = {
    try {
      PInt(value.toInt)
    } catch {
      case _: Exception =>
        try {
          PInt(parseBigDecimal(value).toIntExact)
        } catch {
          case e: Exception =>
            throw JsonFormatException(s"Not an int32 value: $value", e)
        }
    }
  }

  def parseInt64(value: String): PValue = {
    try {
      PLong(value.toLong)
    } catch {
      case _: Exception =>
        val bd = parseBigDecimal(value)
        try {
          PLong(bd.toLongExact)
        } catch {
          case e: Exception =>
            throw JsonFormatException(s"Not an int64 value: $value", e)
        }
    }
  }

  def parseUint32(value: String): PValue = {
    try {
      val result = value.toLong
      if (result < 0 || result > 0xFFFFFFFFl)
        throw new JsonFormatException(s"Out of range uint32 value: $value")
      return PInt(result.toInt)
    } catch {
      case e: JsonFormatException => throw e
      case _: Exception => // Fall through.
    }
    parseBigDecimal(value).toBigIntExact().map { intVal =>
      if (intVal < 0 || intVal > 0xFFFFFFFFl)
        throw new JsonFormatException(s"Out of range uint32 value: $value")
      PLong(intVal.intValue())
    } getOrElse {
      throw new JsonFormatException(s"Not an uint32 value: $value")
    }
  }

  val MAX_UINT64 = BigInt("FFFFFFFFFFFFFFFF", 16)

  def parseUint64(value: String): PValue = {
    parseBigDecimal(value).toBigIntExact().map { intVal =>
      if (intVal < 0 || intVal > MAX_UINT64) {
        throw new JsonFormatException(s"Out of range uint64 value: $value")
      }
      PLong(intVal.longValue())
    } getOrElse {
      throw new JsonFormatException(s"Not an uint64 value: $value")
    }
  }

  def jsonName(fd: FieldDescriptor): String = {
    // protoc<3 doesn't know about json_name, so we fill it in if it's not populated.
    fd.asProto.jsonName.getOrElse(NameUtils.snakeCaseToCamelCase(fd.asProto.getName))
  }
}
