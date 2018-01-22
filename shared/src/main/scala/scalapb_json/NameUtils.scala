package scalapb_json

object NameUtils {
  def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new java.lang.StringBuilder()
    @annotation.tailrec
    def inner(name: String, index: Int, capNext: Boolean): Unit = if (name.nonEmpty) {
      val (r, capNext2) = name.head match {
        case c if c.isLower => (Some(if (capNext) c.toUpper else c), false)
        case c if c.isUpper =>
          // force first letter to lower unless forced to capitalize it.
          (Some(if (index == 0 && !capNext) c.toLower else c), false)
        case c if c.isDigit => (Some(c), true)
        case _ => (None, true)
      }
      r.foreach(b.append)
      inner(name.tail, index + 1, capNext2)
    }
    inner(name, 0, upperInitial)
    b.toString
  }

  private[this] def isLower(c: Char): Boolean = {
    'a' <= c && c <= 'z'
  }

  private[this] def isUpper(c: Char): Boolean = {
    'A' <= c && c <= 'Z'
  }

  private[this] def toUpper(c: Char): Char = {
    if (isLower(c)) {
      (c - 32).asInstanceOf[Char]
    } else {
      c
    }
  }

  private[this] def toLower(c: Char): Char = {
    if (isUpper(c)) {
      (c + 32).asInstanceOf[Char]
    } else {
      c
    }
  }

  private[this] def toLowerCase(s: String, b: Appendable): Unit = {
    @annotation.tailrec
    def loop(i: Int): Unit = {
      if (i < s.length) {
        b.append(toLower(s(i)))
        loop(i + 1)
      }
    }

    loop(0)
  }

  def lowerSnakeCaseToCamelCase(name: String): String = {
    lowerSnakeCaseToCamelCaseWithBuffer(name, new java.lang.StringBuilder(name.length)).toString
  }

  def lowerSnakeCaseToCamelCaseWithBuffer(name: String, buf: Appendable): buf.type = {
    def toProperCase(s: String): Unit = if (!s.isEmpty) {
      buf.append(toUpper(s(0)))
      toLowerCase(s.substring(1), buf)
    }

    val array = name.split("\\_")
    toLowerCase(array(0), buf)

    @annotation.tailrec
    def loop(i: Int): Unit = {
      if (i < array.length) {
        toProperCase(array(i))
        loop(i + 1)
      }
    }

    loop(1)
    buf
  }

  def camelCaseToSnakeCase(str: String): String = {
    if (str.isEmpty) {
      ""
    } else {
      val buf = new java.lang.StringBuilder(str.length)
      buf.append(toLower(str(0)))

      @annotation.tailrec
      def loop(i: Int): String = {
        if (i < str.length) {
          val c = str(i)
          if (isUpper(c)) {
            buf.append('_')
            buf.append((c + 32).asInstanceOf[Char])
          } else {
            buf.append(c)
          }
          loop(i + 1)
        } else {
          buf.toString
        }
      }

      loop(1)
    }
  }
}
