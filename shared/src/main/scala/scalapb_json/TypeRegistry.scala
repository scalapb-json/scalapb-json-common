package scalapb_json

import scalapb._

import ScalapbJsonCommon.GenericCompanion

/** TypeRegistry is used to map the @type field in Any messages to a ScalaPB generated message.
 *
 * You need to
 */
case class TypeRegistry(
  companions: Map[String, GenericCompanion] = Map.empty,
  private val filesSeen: Set[String] = Set.empty) {
  def addMessage[T <: GeneratedMessage with Message[T]](
    implicit cmp: GeneratedMessageCompanion[T]): TypeRegistry = {
    addMessageByCompanion(cmp)
  }

  def addFile(file: GeneratedFileObject): TypeRegistry = {
    if (filesSeen.contains(file.scalaDescriptor.fullName)) this
    else {
      val withFileSeen = copy(filesSeen = filesSeen + file.scalaDescriptor.fullName)

      val withDeps: TypeRegistry =
        file.dependencies.foldLeft(withFileSeen)((r, f) => r.addFile(f))

      file.messagesCompanions.foldLeft(withDeps)((r, mc) =>
        r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
    }
  }

  def addMessageByCompanion(cmp: GenericCompanion): TypeRegistry = {
    // TODO: need to add contained file to follow JsonFormat
    val withNestedMessages =
      cmp.nestedMessagesCompanions.foldLeft(this)((r, mc) =>
        r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
    copy(
      companions = withNestedMessages.companions + ((TypeRegistry.TypePrefix + cmp.scalaDescriptor.fullName) -> cmp))
  }

  def findType(typeName: String): Option[GenericCompanion] = companions.get(typeName)
}

object TypeRegistry {
  private val TypePrefix = "type.googleapis.com/"

  def empty = TypeRegistry(Map.empty)
}
