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
    fieldNameMap.computeIfAbsent(
      descriptor,
      { _ =>
        val mapBuilder = Map.newBuilder[String, FieldDescriptor]
        descriptor.fields.foreach { fd =>
          mapBuilder += fd.name -> fd
          mapBuilder += ScalapbJsonCommon.jsonName(fd) -> fd
        }
        mapBuilder.result()
      }
    )
  }
}
