package scalapb_json

import scalapb.descriptors.{Descriptor, FieldDescriptor}

/**
 * Given a message descriptor, provides a map from field names to field descriptors.
 */
final class MemorizedFieldNameMap(
  val fieldNameMap: scala.collection.mutable.Map[Descriptor, Map[String, FieldDescriptor]]
) {
  def this() = this(scala.collection.mutable.Map.empty)

  def get(descriptor: Descriptor): Map[String, FieldDescriptor] = {
    fieldNameMap.getOrElseUpdate(
      descriptor, {
        val mapBuilder = Map.newBuilder[String, FieldDescriptor]
        descriptor.fields.foreach { fd =>
          mapBuilder += fd.name -> fd
          mapBuilder += ScalapbJsonCommon.jsonName(fd) -> fd
        }
        val value = mapBuilder.result()
        fieldNameMap.put(descriptor, value)
        value
      }
    )
  }
}
