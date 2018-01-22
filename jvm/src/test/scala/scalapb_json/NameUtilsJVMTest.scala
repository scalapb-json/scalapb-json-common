package scalapb_json

import scalaprops._
import com.google.common.base.CaseFormat

object NameUtilsJVMTest extends Scalaprops {

  // https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#isLowerCase-char-
  // https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#isUpperCase-char-
  private[this] val strGen: Gen[String] = Gen.genString(
    Gen.oneOf(
      Gen.asciiChar,
      Gen.elements(
        '\u00E0', '\u00E1', '\u00E2', '\u00E3', '\u00E4', '\u00E5', '\u00E6'
      ),
      Gen.elements(
        '\u00C0', '\u00C1', '\u00C2', '\u00C3', '\u00C4', '\u00C5', '\u00C6'
      )
    )
  )

  val `camelCaseToSnakeCase compatible guava` = Property.forAllG(strGen) { s =>
    // https://github.com/google/protobuf/blob/47b7d2c7ca/java/util/src/main/java/com/google/protobuf/util/FieldMaskUtil.java#L175
    val x = NameUtils.camelCaseToSnakeCase(s)
    val y = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s)
    utest.assert(x == y)
    true
  }

  val `lowerSnakeCaseToCamelCase compatible guava` = Property.forAllG(strGen) { s =>
    // https://github.com/google/protobuf/blob/47b7d2c7ca/java/util/src/main/java/com/google/protobuf/util/FieldMaskUtil.java#L159
    val x = NameUtils.lowerSnakeCaseToCamelCase(s)
    val y = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, s)
    utest.assert(x == y)
    true
  }

}
