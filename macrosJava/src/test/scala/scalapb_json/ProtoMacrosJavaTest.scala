package scalapb_json

import com.google.protobuf.InvalidProtocolBufferException
import jsontest.test.MyTest
import org.scalatest.FunSpec
import org.scalatest.Matchers
import scalapb_json.ProtoMacrosJava._

class ProtoMacrosJavaTest extends FunSpec with Matchers {
  describe("ProtoMacrosJava") {
    it("fromJson") {
      assert(MyTest.fromJson("{}") === MyTest())
      assert(
        MyTest.fromJson(
          """{
            "hello" : "foo",
            "foobar" : 42
          }"""
        ) === MyTest().update(
          _.hello := "foo",
          _.foobar := 42
        )
      )
      """ jsontest.test.MyTest.fromJson("{") """ shouldNot compile
    }
  }
}
