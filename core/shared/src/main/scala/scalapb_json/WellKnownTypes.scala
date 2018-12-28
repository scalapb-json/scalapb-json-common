package scalapb_json

import com.google.protobuf.duration.Duration

object WellKnownTypes {
  // Timestamp for "0001-01-01T00:00:00Z"
  val TIMESTAMP_SECONDS_MIN = -62135596800L

  val TIMESTAMP_SECONDS_MAX = 253402300799L

  val NANOS_PER_SECOND = 1000000000
  val NANOS_PER_MILLISECOND = 1000000
  val NANOS_PER_MICROSECOND = 1000

  @deprecated("will be removed", "")
  val MILLIS_PER_SECOND = 1000
  @deprecated("will be removed", "")
  val MICROS_PER_SECOND = 1000000
  @deprecated("will be removed. use Durations.DURATION_SECONDS_MIN", "")
  val DURATION_SECONDS_MIN = Durations.DURATION_SECONDS_MIN
  @deprecated("will be removed. use Durations.DURATION_SECONDS_MAX", "")
  val DURATION_SECONDS_MAX = Durations.DURATION_SECONDS_MAX

  @deprecated("will be removed. use Durations.checkValid", "")
  def checkValid(duration: com.google.protobuf.duration.Duration): Unit =
    Durations.checkValid(duration)

  def formatNanos(nanos: Int): String = {
    // Determine whether to use 3, 6, or 9 digits for the nano part.
    if (nanos % NANOS_PER_MILLISECOND == 0) {
      "%1$03d".format(nanos / NANOS_PER_MILLISECOND)
    } else if (nanos % NANOS_PER_MICROSECOND == 0) {
      "%1$06d".format(nanos / NANOS_PER_MICROSECOND)
    } else {
      "%1$09d".format(nanos)
    }
  }

  @deprecated("will be removed. use Durations.writeDuration", "")
  def writeDuration(duration: com.google.protobuf.duration.Duration): String =
    Durations.writeDuration(duration)

  @deprecated("will be removed. use Durations.parseNanos", "")
  def parseNanos(value: String): Int =
    Durations.parseNanos(value)

  @deprecated("will be removed. use Durations.parseDuration", "")
  def parseDuration(value: String): Duration =
    Durations.parseDuration(value)
}
