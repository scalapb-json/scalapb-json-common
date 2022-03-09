package scalapb_json

import com.google.protobuf.struct.NullValue
import com.google.protobuf.struct.ListValue
import com.google.protobuf.struct.Struct
import com.google.protobuf.struct.Value
import scala.quoted.Expr
import scala.quoted.ToExpr
import scala.quoted.Quotes

object ProtoMacrosCommon {

  implicit val protoNullValueToExpr: ToExpr[NullValue] =
    new ToExpr[NullValue] {
      def apply(v: NullValue)(using Quotes) = '{
        NullValue.fromValue(${ Expr(v.value) })
      }
    }

  implicit val protoStructToExpr: ToExpr[Struct] =
    new ToExpr[Struct] {
      def apply(v: Struct)(using Quotes) = '{
        Struct(${ Expr(v.fields) })
      }
    }

  implicit val protoListValueToExpr: ToExpr[ListValue] =
    new ToExpr[ListValue] {
      def apply(v: ListValue)(using Quotes) = '{
        ListValue(${
          Expr.ofList(v.values.map(summon[ToExpr[Value]].apply))
        })
      }
    }

  implicit val protoValueToExpr: ToExpr[Value] =
    new ToExpr[Value] {
      def apply(v: Value)(using Quotes) = '{
        Value(
          ${
            v.kind match {
              case Value.Kind.Empty =>
                '{ Value.Kind.Empty }
              case Value.Kind.NullValue(value) =>
                '{ Value.Kind.NullValue(${ Expr(value) }) }
              case Value.Kind.NumberValue(value) =>
                '{ Value.Kind.NumberValue(${ Expr(value) }) }
              case Value.Kind.StringValue(value) =>
                '{ Value.Kind.StringValue(${ Expr(value) }) }
              case Value.Kind.BoolValue(value) =>
                '{ Value.Kind.BoolValue(${ Expr(value) }) }
              case Value.Kind.StructValue(value) =>
                '{ Value.Kind.StructValue(${ Expr(value) }) }
              case Value.Kind.ListValue(value) =>
                '{ Value.Kind.ListValue(${ Expr(value) }) }
            }
          }
        )
      }
    }
}
