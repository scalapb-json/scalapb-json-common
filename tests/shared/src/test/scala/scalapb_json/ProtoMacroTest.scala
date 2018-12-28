package scalapb_json

import org.scalatest.FunSpec
import org.scalatest.Matchers
import scalapb_json.ProtoMacros._
import scalapb_json.ProtoMacrosJava._
import com.google.protobuf.struct._

class ProtoMacroTest extends FunSpec with Matchers {
  describe("ProtoMacro") {
    it("struct") {
      assert(struct"{}" == Struct.defaultInstance)

      assert(
        struct"""{ "a" : [1, false, null, "x"] }""" == Struct(
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

      """ struct" { invalid json " """ shouldNot compile
    }

    it("value") {
      assert(value" 42 " == Value(Value.Kind.NumberValue(42)))
      assert(value" false " == Value(Value.Kind.BoolValue(false)))
      assert(value""" "x" """ == Value(Value.Kind.StringValue("x")))
      assert(value""" [] """ == Value(Value.Kind.ListValue(ListValue())))

      """ value" { invalid json " """ shouldNot compile
    }
  }
}
