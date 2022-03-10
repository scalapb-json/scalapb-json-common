package scalapb_json

import scalapb_json.ProtoMacrosCommon.*
import com.google.protobuf.struct.Value
import com.google.protobuf.struct.Struct
import com.google.protobuf.struct.ListValue
import com.google.protobuf.struct.NullValue
import scalaprops.Gen
import scalaprops.Property
import scalaprops.Scalaprops
import scala.quoted.Expr
import scala.quoted.FromExpr
import scala.quoted.ToExpr
import scala.quoted.Quotes
import scala.quoted.staging.{run, withQuotes, Compiler}

object ProtoMacrosCommonTest extends Scalaprops {

  // def run[T](expr: Quotes ?=> Expr[T])(using compiler: Compiler): T
  // def withQuotes[T](thunk: Quotes ?=> T)(using compiler: Compiler): T

  val test: Property = {
    given Compiler = Compiler.make(getClass.getClassLoader)

    val x1: Quotes => Property = run(testImpl)
    val x2: Quotes ?=> Property = x1.apply(summon[Quotes])
    withQuotes(x2)
  }

  def testImpl(using q: Quotes): Expr[Quotes => Property] =
    '{
      val primitive: Gen[Value] = Gen
        .oneOf(
          Gen.value(Value.Kind.Empty),
//          Gen.value(Value.Kind.NullValue(NullValue.NULL_VALUE)),
//          Gen[Int].map(a => Value.Kind.NullValue(NullValue.fromValue(a))),
          Gen[Double]
            .map(x => if (x.isNaN) Value.Kind.NumberValue(0) else Value.Kind.NumberValue(x)),
          Gen.alphaNumString.map(Value.Kind.StringValue.apply),
          Gen[Boolean].map(Value.Kind.BoolValue.apply),
        )
        .map(Value.apply(_))

      val list1: Gen[ListValue] = Gen.listOf(primitive).map(ListValue.apply(_))
      val struct1: Gen[Struct] = Gen.mapGen(Gen.alphaNumString, primitive).map(Struct.apply(_))

      implicit val value1: Gen[Value] =
        Gen.oneOf(
          primitive,
          list1.map(a => Value(Value.Kind.ListValue(a))),
          struct1.map(a => Value(Value.Kind.StructValue(a))),
        )

      { implicit q2: Quotes =>
        {
          Property.forAll { (x1: Value) =>
            println(List("*" * 50, x1, Expr(x1).show, "*" * 50).mkString("\n", "\n", "\n"))
            val x3 = summon[FromExpr[Value]].unapply(Expr(x1))
            assert(Some(x1) == x3, s"$x1 != $x3")
            true
          }
        }
      }
    }

}
