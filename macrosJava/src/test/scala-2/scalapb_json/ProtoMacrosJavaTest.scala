package scalapb_json

import jsontest.test.MyTest
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import scalapb_json.ProtoMacrosJava._
import scala.util.Success

class ProtoMacrosJavaTest extends AnyFunSpec with Matchers {
  describe("ProtoMacrosJava") {
    it("fromJson") {
      assert(MyTest.fromJsonConstant("{}") === MyTest())
      assert(
        MyTest.fromJsonConstant(
          """{
            "hello" : "foo",
            "foobar" : 42
          }"""
        ) === MyTest().update(
          _.hello := "foo",
          _.foobar := 42
        )
      )
      """ jsontest.test.MyTest.fromJsonConstant("{") """ shouldNot compile

      assert(MyTest.fromJsonTry("{").isFailure)
      assert(MyTest.fromJsonTry("""{"hello":"foo"}""") === Success(MyTest(hello = Some("foo"))))

      assert(MyTest.fromJsonOpt("{").isEmpty)
      assert(MyTest.fromJsonOpt("""{"hello":"foo"}""") === Some(MyTest(hello = Some("foo"))))

      assert(MyTest.fromJsonEither("{").isLeft)
      assert(MyTest.fromJsonEither("""{"hello":"foo"}""") === Right(MyTest(hello = Some("foo"))))
    }
  }
}
