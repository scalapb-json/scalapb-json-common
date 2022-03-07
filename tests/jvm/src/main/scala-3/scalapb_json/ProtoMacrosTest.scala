package scalapb_json

import com.google.protobuf.struct.{ListValue, NullValue, Struct, Value}
import scalapb_json.ProtoMacros.*
import scala.compiletime.testing.{typeCheckErrors, ErrorKind}

object ProtoMacrosTest {
  def main(args: Array[String]): Unit = {

    typeCheckErrors(""" com.google.protobuf.struct.Struct.fromTextFormat("a") """) match {
      case List(err) =>
        assert(err.kind == ErrorKind.Typer)
        assert(err.message.contains(""" Expected "{"."""), err.message)
      case other =>
        sys.error(s"unexpected error ${other}")
    }

    val b = "b"

    typeCheckErrors(""" com.google.protobuf.struct.Struct.fromTextFormat(b) """) match {
      case List(err) =>
        assert(err.kind == ErrorKind.Typer)
        assert(err.message.contains("""expect String literal"""), err.message)
      case other =>
        sys.error(s"unexpected error ${other}")
    }

    val result = com.google.protobuf.struct.Struct.fromTextFormat(
      """fields {
        key: "a"
        value {
          list_value {
            values {
              number_value: 1.0
            }
            values {
              bool_value: false
            }
            values {
              null_value: NULL_VALUE
            }
            values {
              string_value: "x"
            }
          }
        }
      }"""
    )
    assert(
      result == Struct(
        Map(
          "a" -> Value(
            Value.Kind.ListValue(
              ListValue(
                List(
                  Value(Value.Kind.NumberValue(1.0)),
                  Value(Value.Kind.BoolValue(false)),
                  Value(Value.Kind.NullValue(NullValue.NULL_VALUE)),
                  Value(Value.Kind.StringValue("x"))
                )
              )
            )
          )
        )
      )
    )
  }
}
