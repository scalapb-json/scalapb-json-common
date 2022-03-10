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
import scala.quoted.staging.Compiler
import scala.quoted.staging.run
import scala.quoted.staging.withQuotes

object ProtoMacrosCommonTest extends Scalaprops {

  val test: Property = {
    given Compiler = Compiler.make(getClass.getClassLoader)
    val x1: Quotes => Property = run(testImpl)
    val x2: Quotes ?=> Property = x1.apply(summon[Quotes])
    withQuotes(x2)
  }

  private[this] def testImpl(using Quotes): Expr[Quotes => Property] =
    '{
      val primitive: Gen[Value] = Gen
        .oneOf(
          Gen.value(Value.Kind.Empty),
          Gen.value(Value.Kind.NullValue(NullValue.NULL_VALUE)),
          Gen[Int].map(a => Value.Kind.NullValue(NullValue.fromValue(a))),
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

      { implicit q: Quotes =>
        {
          Property.forAll { (x1: Value) =>
            val x2 = summon[FromExpr[Value]].unapply(Expr(x1))
            assert(Some(x1) == x2, s"$x1 != $x2")
            true
          }
        }
      }
    }
}
