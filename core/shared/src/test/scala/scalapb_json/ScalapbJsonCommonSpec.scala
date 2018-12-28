package scalapb_json

import com.google.protobuf.field_mask.FieldMask
import utest._

object ScalapbJsonCommonSpec extends utest.TestSuite {

  override def tests = Tests {
    "FieldMask" - {
      // https://github.com/google/protobuf/blob/47b7d2c7cad/java/util/src/test/java/com/google/protobuf/util/JsonFormatTest.java#L761-L770
      val x = FieldMask(Seq("foo.bar", "baz", "foo_bar.baz"))
      val expect = "foo.bar,baz,fooBar.baz"
      val json = ScalapbJsonCommon.fieldMaskToJsonString(x)
      assert(json == expect)
    }
  }

}
