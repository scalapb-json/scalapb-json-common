package scalapb_json

import com.google.protobuf.timestamp.Timestamp
import utest._

object TimestampSpec extends TestSuite {

  val tests = Tests {
    "Timestamp parser should work" - {
      val start = Timestamps.parseTimestamp("0001-01-01T00:00:00Z")
      val end = Timestamps.parseTimestamp("9999-12-31T23:59:59.999999999Z")
      assert(start.seconds == Timestamps.TIMESTAMP_SECONDS_MIN)
      assert(start.nanos == 0)
      assert(end.seconds == Timestamps.TIMESTAMP_SECONDS_MAX)
      assert(end.nanos == 999999999)

      assert(Timestamps.writeTimestamp(start) == "0001-01-01T00:00:00Z")
      assert(Timestamps.writeTimestamp(end) == "9999-12-31T23:59:59.999999999Z")

      assert(Timestamps.parseTimestamp("1970-01-01T00:00:00Z") == Timestamp(0, 0))
      assert(Timestamps.parseTimestamp("1969-12-31T23:59:59.999Z") == Timestamp(-1, 999000000))

      assert(Timestamps.writeTimestamp(Timestamp(nanos = 10)) == "1970-01-01T00:00:00.000000010Z")
      assert(Timestamps.writeTimestamp(Timestamp(nanos = 10000)) == "1970-01-01T00:00:00.000010Z")
      assert(Timestamps.writeTimestamp(Timestamp(nanos = 10000000)) == "1970-01-01T00:00:00.010Z")

      assert(Timestamps.writeTimestamp(Timestamps.parseTimestamp("1970-01-01T00:00:00.010+08:35")) ==
        "1969-12-31T15:25:00.010Z")
      assert(Timestamps.writeTimestamp(Timestamps.parseTimestamp("1970-01-01T00:00:00.010-08:12")) ==
        "1970-01-01T08:12:00.010Z")
    }
  }
}
