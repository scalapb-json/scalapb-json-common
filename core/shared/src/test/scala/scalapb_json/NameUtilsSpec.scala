package scalapb_json

import utest._

object NameUtilsSpec extends TestSuite {
  val tests = Tests {
    "snakeCaseToCamelCase should work for normal names" - {
      assert(NameUtils.snakeCaseToCamelCase("scala_pb") == "scalaPb")
      assert(NameUtils.snakeCaseToCamelCase("foo_bar") == "fooBar")
      assert(NameUtils.snakeCaseToCamelCase("foo_bar_123baz") == "fooBar123Baz")
      assert(NameUtils.snakeCaseToCamelCase("foo_bar_123_baz") == "fooBar123Baz")
      assert(NameUtils.snakeCaseToCamelCase("__foo_bar") == "FooBar")
      assert(NameUtils.snakeCaseToCamelCase("_foo_bar") == "FooBar")
      assert(NameUtils.snakeCaseToCamelCase("_scala_pb") == "ScalaPb")
      assert(NameUtils.snakeCaseToCamelCase("foo__bar") == "fooBar")
      assert(NameUtils.snakeCaseToCamelCase("123bar") == "123Bar")
      assert(NameUtils.snakeCaseToCamelCase("123_bar") == "123Bar")
    }

    "snakeCaseToCamelCase should work when already in camel case" - {
      assert(NameUtils.snakeCaseToCamelCase("fooBar") == "fooBar")
      assert(NameUtils.snakeCaseToCamelCase("fooBar_baz") == "fooBarBaz")
      assert(NameUtils.snakeCaseToCamelCase("FooBar") == "fooBar")
    }
  }
}
