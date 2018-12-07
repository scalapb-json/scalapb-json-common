import scalapb.compiler.Version._
import sbtrelease.ReleaseStateTransformations._
import sbtcrossproject.CrossPlugin.autoImport.crossProject

val Scala211 = "2.11.12"

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}

val unusedWarnings = Seq("-Ywarn-unused", "-Ywarn-unused-import")

val scalapbJsonCommon = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    commonSettings,
    mappings in (Compile, packageSrc) ++= (managedSources in Compile).value.map { f =>
      // https://github.com/sbt/sbt-buildinfo/blob/v0.7.0/src/main/scala/sbtbuildinfo/BuildInfoPlugin.scala#L58
      val buildInfoDir = "sbt-buildinfo"
      val path = if (f.getAbsolutePath.contains(buildInfoDir)) {
        (file(buildInfoPackage.value) / f
          .relativeTo((sourceManaged in Compile).value / buildInfoDir)
          .get
          .getPath).getPath
      } else {
        f.relativeTo((sourceManaged in Compile).value).get.getPath
      }
      (f, path)
    },
    buildInfoPackage := "scalapb_json",
    buildInfoObject := "ScalapbJsonCommonBuildInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      "scalapbVersion" -> scalapbVersion,
      scalaVersion,
      version
    )
  )
  .jvmSettings(
    PB.targets in Test := Seq(
      PB.gens.java -> (sourceManaged in Test).value,
      scalapb.gen(javaConversions = true) -> (sourceManaged in Test).value
    ),
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java-util" % protobufVersion % "test"
    )
  )
  .nativeSettings(
    crossScalaVersions := Scala211 :: Nil,
    nativeLinkStubs := true
  )
  .jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalapb-json/scalapb-json-common/" + tagOrHash.value
      s"-P:scalajs:mapSourceURI:$a->$g/"
    },
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-RC1"
    )
  )
  .platformsSettings(JVMPlatform, JSPlatform)(
    // TODO enable in scala-native https://github.com/scalaprops/sbt-scalaprops/issues/4
    scalapropsCoreSettings,
    libraryDependencies += "com.github.scalaprops" %%% "scalaprops" % "0.5.5" % "test",
    Seq((Compile, "main"), (Test, "test")).map {
      case (x, y) =>
        unmanagedSourceDirectories in x += {
          baseDirectory.value.getParentFile / s"jvm_js/src/${y}/scala/"
        }
    }
  )
  .platformsSettings(JSPlatform, NativePlatform)(
    unmanagedSourceDirectories in Compile += {
      baseDirectory.value.getParentFile / "js_native/src/main/scala/"
    },
    PB.targets in Test := Seq(
      scalapb.gen(javaConversions = false) -> (sourceManaged in Test).value
    )
  )

commonSettings

val noPublish = Seq(
  PgpKeys.publishLocalSigned := {},
  PgpKeys.publishSigned := {},
  publishLocal := {},
  publish := {},
  publishArtifact in Compile := false
)

noPublish

lazy val commonSettings = Seq[Def.SettingsDefinition](
  unmanagedResources in Compile += (baseDirectory in LocalRootProject).value / "LICENSE.txt",
  scalaVersion := Scala211,
  crossScalaVersions := Seq("2.12.7", Scala211, "2.10.7"),
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, v)) if v >= 11 => unusedWarnings
    }
    .toList
    .flatten,
  Seq(Compile, Test).flatMap(c => scalacOptions in (c, console) --= unusedWarnings),
  scalacOptions ++= Seq("-feature", "-deprecation", "-language:existentials"),
  description := "Json/Protobuf convertors for ScalaPB",
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  organization := "io.github.scalapb-json",
  name := UpdateReadme.scalapbJsonCommonName,
  Project.inConfig(Test)(sbtprotoc.ProtocPlugin.protobufConfigSettings),
  PB.targets in Compile := Nil,
  PB.protoSources in Test := Seq(file("shared/src/test/protobuf")),
  libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapbVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf,test",
    "com.lihaoyi" %%% "utest" % "0.6.6" % "test"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  pomExtra in Global := {
    <url>https://github.com/scalapb-json/scalapb-json-common</url>
      <scm>
        <connection>scm:git:github.com/scalapb-json/scalapb-json-common.git</connection>
        <developerConnection>scm:git:git@github.com:scalapb-json/scalapb-json-common.git</developerConnection>
        <url>github.com/scalapb-json/scalapb-json-common.git</url>
        <tag>{tagOrHash.value}</tag>
      </scm>
      <developers>
        <developer>
          <id>xuwei-k</id>
          <name>Kenji Yoshida</name>
          <url>https://github.com/xuwei-k</url>
        </developer>
      </developers>
  },
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  scalacOptions in (Compile, doc) ++= {
    val t = tagOrHash.value
    Seq(
      "-sourcepath",
      (baseDirectory in LocalRootProject).value.getAbsolutePath,
      "-doc-source-url",
      s"https://github.com/scalapb-json/scalapb-json-common/tree/${t}€{FILE_PATH}.scala"
    )
  },
  ReleasePlugin.extraReleaseCommands,
  commands += Command.command("updateReadme")(UpdateReadme.updateReadmeTask),
  releaseTagName := tagName.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    UpdateReadme.updateReadmeProcess,
    tagRelease,
    ReleaseStep(
      action = { state =>
        val extracted = Project extract state
        extracted.runAggregated(
          PgpKeys.publishSigned in Global in extracted.get(thisProjectRef),
          state)
      },
      enableCrossBuild = true
    ),
    releaseStepCommandAndRemaining(s"; ++ ${Scala211}! ; scalapbJsonCommonNative/publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    UpdateReadme.updateReadmeProcess,
    pushChanges
  )
).flatMap(_.settings)

val scalapbJsonCommonJVM = scalapbJsonCommon.jvm
val scalapbJsonCommonJS = scalapbJsonCommon.js
val scalapbJsonCommonNative = scalapbJsonCommon.native

val root = project
  .in(file("."))
  .settings(
    commonSettings,
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    PgpKeys.publishSigned := {},
    PgpKeys.publishLocalSigned := {}
  )
  .aggregate(
    scalapbJsonCommonJVM,
    scalapbJsonCommonJS
    // exclude Native on purpose
  )
