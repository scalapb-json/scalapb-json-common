package scalapb_json

import scalapb.descriptors.{Descriptor, FieldDescriptor}
import java.util.concurrent.ConcurrentHashMap

/**
 * Given a message descriptor, provides a map from field names to field descriptors.
 *
 * @note use java `ConcurrentHashMap` because `TrieMap` is not available on scala-js
 */
final class MemorizedFieldNameMap(
  val fieldNameMap: ConcurrentHashMap[Descriptor, Map[String, FieldDescriptor]]
) {
  def this() = this(new ConcurrentHashMap())

  def get(descriptor: Descriptor): Map[String, FieldDescriptor] = {
    // use `get` and `put` instead of `computeIfAbsent` on purpose because
    // - can't use `SAM conversion` with Scala 2.11
    // - does not need strict consistency in this case
    fieldNameMap.get(descriptor) match {
      case null =>
        val mapBuilder = Map.newBuilder[String, FieldDescriptor]
        descriptor.fields.foreach { fd =>
          mapBuilder += fd.name -> fd
          mapBuilder += ScalapbJsonCommon.jsonName(fd) -> fd
        }
        val value = mapBuilder.result()
        fieldNameMap.put(descriptor, value)
        value
      case value =>
        value
    }
  }
}
