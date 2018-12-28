package scalapb_json

import com.google.protobuf.struct.{Struct, Value}
import scalapb.GeneratedMessageCompanion

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

abstract class ProtoMacrosCommon(val c: blackbox.Context) {

  import c.universe._

  def fromJsonOptImpl(json: c.Tree): c.Tree = {
    q"""try{
      _root_.scala.Some(${fromJsonImpl0(json)})
    } catch {
      case _: _root_.com.google.protobuf.InvalidProtocolBufferException =>
        _root_.scala.None
    }"""
  }

  def fromJsonEitherImpl(json: c.Tree): c.Tree = {
    val error = TermName(c.freshName)
    q"""try{
      _root_.scala.Right(${fromJsonImpl0(json)})
    } catch {
      case $error : _root_.com.google.protobuf.InvalidProtocolBufferException =>
        _root_.scala.Left($error)
    }"""
  }

  def fromJsonTryImpl(json: c.Tree): c.Tree = {
    val error = TermName(c.freshName)
    q"""try{
      _root_.scala.util.Success(${fromJsonImpl0(json)})
    } catch {
      case $error: _root_.com.google.protobuf.InvalidProtocolBufferException =>
        _root_.scala.util.Failure($error)
    }"""
  }

  def fromJsonDebugImpl(json: c.Tree): c.Tree = {
    val code = fromJsonImpl0(json)
    println(showCode(code))
    code
  }

  def fromJsonImpl0[A <: scalapb.GeneratedMessage with scalapb.Message[A]: c.WeakTypeTag](
    json: c.Tree
  ): c.Tree = {
    val Literal(Constant(str: String)) = json
    val clazz = Class.forName(weakTypeOf[A].toString + "$")
    implicit val c: GeneratedMessageCompanion[A] =
      clazz
        .getField(scala.reflect.NameTransformer.MODULE_INSTANCE_NAME)
        .get(null)
        .asInstanceOf[GeneratedMessageCompanion[A]]
    fromJsonImpl[A](str)
  }

  def fromJsonImpl[
    A <: scalapb.GeneratedMessage with scalapb.Message[A]: c.WeakTypeTag: GeneratedMessageCompanion
  ](string: String): c.Tree

  def protoStructInterpolation(): c.Tree =
    c.prefix.tree match {
      case Apply(_, List(Apply(_, List(Literal(Constant(str: String)))))) =>
        protoString2Struct(str)
    }

  def protoValueInterpolation(): c.Tree =
    c.prefix.tree match {
      case Apply(_, List(Apply(_, List(Literal(Constant(str: String)))))) =>
        protoString2Value(str)
    }

  protected[this] def protoString2Value(string: String): c.Tree

  protected[this] def protoString2Struct(string: String): c.Tree

  implicit val NullValueLiftable: Liftable[com.google.protobuf.struct.NullValue] =
    new Liftable[com.google.protobuf.struct.NullValue] {

      import com.google.protobuf.struct.NullValue

      override def apply(value: NullValue) = value match {
        case NullValue.NULL_VALUE =>
          q"_root_.com.google.protobuf.struct.NullValue.NULL_VALUE"
        case NullValue.Unrecognized(v) =>
          q"_root_.com.google.protobuf.struct.NullValue.Unrecognized($v)"
      }
    }

  implicit val StructLiftable: Liftable[Struct] =
    new Liftable[Struct] {
      override def apply(value: Struct) = {
        val v = value.fields
        q"_root_.com.google.protobuf.struct.Struct($v)"
      }
    }

  implicit val ValueLiftable: Liftable[Value] = new Liftable[Value] {

    import com.google.protobuf.struct.Value.Kind._

    override def apply(v: Value) = {
      val x = v.kind match {
        case Empty =>
          q"_root_.com.google.protobuf.struct.Value.Kind.Empty"
        case NullValue(value) =>
          q"_root_.com.google.protobuf.struct.Value.Kind.NullValue($value)"
        case NumberValue(value) =>
          q"_root_.com.google.protobuf.struct.Value.Kind.NumberValue($value)"
        case StringValue(value) =>
          q"_root_.com.google.protobuf.struct.Value.Kind.StringValue($value)"
        case BoolValue(value) =>
          q"_root_.com.google.protobuf.struct.Value.Kind.BoolValue($value)"
        case StructValue(value) =>
          q"_root_.com.google.protobuf.struct.Value.Kind.StructValue($value)"
        case ListValue(value) =>
          q"_root_.com.google.protobuf.struct.Value.Kind.ListValue($value)"
      }
      q"_root_.com.google.protobuf.struct.Value($x)"
    }
  }

  implicit val ListValueLiftable: Liftable[com.google.protobuf.struct.ListValue] =
    new Liftable[com.google.protobuf.struct.ListValue] {
      override def apply(value: com.google.protobuf.struct.ListValue) = {
        val v = value.values
        q"_root_.com.google.protobuf.struct.ListValue(_root_.scala.List(..$v))"
      }
    }
}
