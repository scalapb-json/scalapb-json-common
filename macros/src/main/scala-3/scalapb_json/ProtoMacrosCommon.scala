package scalapb_json

import com.google.protobuf.struct.NullValue
import com.google.protobuf.struct.ListValue
import com.google.protobuf.struct.Struct
import com.google.protobuf.struct.Value
import scala.quoted.Expr
import scala.quoted.FromExpr
import scala.quoted.ToExpr
import scala.quoted.Quotes

object ProtoMacrosCommon {

  implicit val protoNullValueFromExpr: FromExpr[NullValue] =
    new FromExpr[NullValue] {
      def unapply(v: Expr[NullValue])(using Quotes) = PartialFunction.condOpt(v) {
        case '{ NullValue.NULL_VALUE } =>
          NullValue.NULL_VALUE
        case '{ NullValue.Unrecognized(${ Expr(int) }) } =>
          NullValue.Unrecognized(int)
      }
    }

  implicit val protoStructFromExpr: FromExpr[Struct] =
    new FromExpr[Struct] {
      def unapply(v: Expr[Struct])(using Quotes) = PartialFunction.condOpt(v) {
        case '{ Struct(${ Expr(value) }) } =>
          Struct(value)
      }
    }

  implicit val protoListFromExpr: FromExpr[ListValue] =
    new FromExpr[ListValue] {
      def unapply(v: Expr[ListValue])(using Quotes) = PartialFunction.condOpt(v) {
        case '{ ListValue(${ Expr(value) }) } =>
          ListValue(value)
      }
    }

  implicit val protoValueFromExpr: FromExpr[Value] =
    new FromExpr[Value] {
      def unapply(v: Expr[Value])(using Quotes) = PartialFunction
        .condOpt(v) {
          case '{ Value(Value.Kind.Empty) } =>
            Value.Kind.Empty
          case '{ Value(Value.Kind.NullValue(${ Expr(value) })) } =>
            Value.Kind.NullValue(value)
          case '{ Value(Value.Kind.NullValue(NullValue.fromValue(${ Expr(value) }))) } =>
            Value.Kind.NullValue(NullValue.fromValue(value))
          case '{ Value(Value.Kind.NumberValue(${ Expr(value) })) } =>
            Value.Kind.NumberValue(value)
          case '{ Value(Value.Kind.StringValue(${ Expr(value) })) } =>
            Value.Kind.StringValue(value)
          case '{ Value(Value.Kind.BoolValue(${ Expr(value) })) } =>
            Value.Kind.BoolValue(value)
          case '{ Value(Value.Kind.StructValue(${ Expr(value) })) } =>
            Value.Kind.StructValue(value)
          case '{ Value(Value.Kind.ListValue(${ Expr(value) })) } =>
            Value.Kind.ListValue(value)
        }
        .map(Value.apply(_))
    }

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
