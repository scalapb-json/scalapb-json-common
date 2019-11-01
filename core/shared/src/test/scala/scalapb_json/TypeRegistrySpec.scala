package scalapb_json

import com.google.protobuf.wrappers.UInt64Value
import jsontest.test3.{MyTest3, Test3Proto, Wrapper}
import utest._

object TypeRegistrySpec extends TestSuite {
  val tests = Tests {
    "addFile should add all messages in the file" - {
      val reg = TypeRegistry().addFile(Test3Proto)

      assert(reg.findType("type.googleapis.com/jsontest.MyTest3") == Some(MyTest3))
      assert(reg.findType("type.googleapis.com/jsontest.Wrapper") == Some(Wrapper))
      assert(reg.findType("type.googleapis.com/google.protobuf.UInt64Value") == Some(UInt64Value))
      assert(reg.findType("type.googleapis.com/something.else") == None)
    }

    "addMessage should not add other messages from same file" - {
      val reg = TypeRegistry().addMessage[MyTest3]
      assert(reg.findType("type.googleapis.com/jsontest.MyTest3") == Some(MyTest3))
      assert(reg.findType("type.googleapis.com/jsontest.Wrapper") == None)
    }
  }
}
