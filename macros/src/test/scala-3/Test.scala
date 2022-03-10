package aaa

import scala.quoted.*
import scala.deriving.Mirror.ProductOf

case class Hoge(x: Int, y: String, z: Boolean)

trait ThisIsProductOf[A] { self =>
}

object Hoge extends ThisIsProductOf[Hoge] {
  implicit def toExprInstance: ToExpr[ProductOf[Hoge]] =
    new ToExpr[ProductOf[Hoge]] {
      def apply(a: ProductOf[Hoge])(using Quotes) =
        '{ Hoge }.asInstanceOf[Expr[ProductOf[Hoge]]]
    }
}

object Test {
  inline def bbb: Hoge = ${ccc}

  def ccc(using Type[Hoge], Quotes) = {
    foo.DeriveExpr.derive[Hoge].apply(
      Hoge(1, "a", false)
    )
  }

}
