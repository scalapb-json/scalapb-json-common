package foo

import scala.quoted.*
import scala.deriving.Mirror
import scala.compiletime.*
//import shapeless3.deriving.K0
//import shapeless3.deriving.K0.*

class SeqProduct(val x: Seq[Any]) extends Product{
  override def productElement(i: Int) = x(i)
  override def canEqual(that: Any): Boolean = true
  override def productArity: Int = x.size
}

object SeqProduct {
  implicit val instance: ToExpr[SeqProduct] =
    new ToExpr[SeqProduct] {
      def apply(x: SeqProduct)(using Quotes) = {
        val values: Seq[String] = x.x.asInstanceOf[Seq[String]]
        '{ new SeqProduct(${Expr(values)}) }
      } 
    }
}

object DeriveExpr {

/*
  given Tuple3ToExpr[T1: Type: ToExpr, T2: Type: ToExpr, T3: Type: ToExpr]: ToExpr[Tuple3[T1, T2, T3]] with {
    def apply(tup: Tuple3[T1, T2, T3])(using Quotes) =
      '{ (${Expr(tup._1)}, ${Expr(tup._2)}, ${Expr(tup._3)}) }
  }


 inline def traverse[G[_]](x: T)(
    map: [a,b] => (G[a], (a => b)) => G[b],
    pure: [a] => a => G[a],
    ap: [a,b] => (G[a => b], G[a]) => G[b],
    k: [t] => (F[t], t) => G[t]
  : G[T] =

*/


  inline def derive[A]: ToExpr[A] =
    summonFrom {
      case m1: ToExpr[A] =>
        m1
      case m1: Mirror.ProductOf[A] =>
        summonFrom {
          case m2: ToExpr[Mirror.ProductOf[A]] =>
            summonFrom {
              case m3: Type[A] =>
                val values = deriveRec[m1.MirroredElemTypes]
                product(values, m1)
            }
        }
    }

  //def product[A: Type](values: List[ToExpr[?]], m: Mirror.ProductOf[A])(using Quotes, ToExpr[Mirror.ProductOf[A]]): ToExpr[A] = {
  def product[A: Type](values: List[ToExpr[?]], m: Mirror.ProductOf[A])(using ToExpr[Mirror.ProductOf[A]]): ToExpr[A] = {
    new ToExpr[A] {
      def apply(a: A)(using Quotes) = {
        type T = Any
        val x1 = values.zip(a.asInstanceOf[Product].productIterator.toList).map {
          case (toExpr, v) =>
            toExpr.asInstanceOf[ToExpr[T]].apply(v.asInstanceOf[T])
        }
        val p = SeqProduct(x1)
        '{ ${Expr(m)}.fromProduct(${Expr(p)}) }
      }
    }
  }

  
  inline def deriveRec[T <: Tuple]: List[ToExpr[?]] =
    inline erasedValue[T] match {
      case _: EmptyTuple =>
        Nil
      case _: (t *: ts) =>
        derive[t] :: deriveRec[ts]
    }

   
    //     inline def project[R](t: T)(p: Int)(f: [t] => (F[t], t) => R): R =

/*
  inline implicit def toExprProduct[A](using inst: => K0.ProductInstances[ToExpr, A]): ToExpr[A] =
    new ToExpr[A] {
      def apply(a: A)(using Quotes): Expr[A] = {
        val x = inst.project[Expr[Any]](a)(0)([t] => (ft: ToExpr[t], t: t) => ft(t))
//        ProductGeneric[A]

        x : Expr[Any]

//        inst.traverse[Expr](a)([x, y] => (x: Expr[x], f: x => y) => new 
    //    val p = inst.map([t] => (y: ToExpr[t], b: t) => y apply b)
        ???
      }
    }

  inline implicit def toExprCoproduct[A](using inst: => K0.CoproductInstances[ToExpr, A]): ToExpr[A] =
    ???
*/


}
