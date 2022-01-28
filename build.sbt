import scalapb.compiler.Version._
import sbtrelease.ReleaseStateTransformations._
import sbtcrossproject.CrossPlugin.autoImport.crossProject

val Scala212 = "2.12.15"
val Scala213 = "2.13.8"
val Scala3 = "3.1.1"
val scalatestVersion = "3.2.11"

val isScala3 = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3)
)

val scalapbV = settingKey[String]("")

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (ThisBuild / version).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}

val unusedWarnings = Seq("-Ywarn-unused")

lazy val disableScala3 = Def.settings(
  libraryDependencies := {
    if (isScala3.value) {
      Nil
    } else {
      libraryDependencies.value
    }
  },
  Seq(Compile, Test).map { x =>
    (x / sources) := {
      if (isScala3.value) {
        Nil
      } else {
        (x / sources).value
      }
    }
  },
  Test / test := {
    if (isScala3.value) {
      ()
    } else {
      (Test / test).value
    }
  },
  publish / skip := isScala3.value,
)

lazy val forkCompilerProject = project.settings(
  commonSettings,
  libraryDependencies += {
    if (isScala3.value) {
      scalaOrganization.value %% "scala3-compiler" % scalaVersion.value
    } else {
      scalaOrganization.value % "scala-compiler" % scalaVersion.value
    }
  },
  run / fork := true,
  Compile / run / mainClass := Some(
    if (isScala3.value) {
      "dotty.tools.dotc.Main"
    } else {
      "scala.tools.nsc.Main"
    }
  ),
  noPublish
)

val forkScalaCompiler = Def.task {
  import java.io.File
  import java.util.Optional
  import xsbti.{AnalysisCallback, FileConverter, Reporter, VirtualFile}
  import xsbti.compile._
  val options = classpathOptions.value
  sbt.internal.inc.ZincUtil.compilers(
    instance = Keys.scalaInstance.value,
    classpathOptions = options,
    javaHome = None,
    new ScalaCompiler {
      override def classpathOptions = options

      override def compile(
        source: Array[VirtualFile],
        classpath: Array[VirtualFile],
        converter: FileConverter,
        changes: DependencyChanges,
        options: Array[String],
        output: Output,
        callback: AnalysisCallback,
        reporter: Reporter,
        progressOpt: Optional[CompileProgress],
        log: xsbti.Logger,
      ): Unit = {
        val dir = output.getSingleOutputAsPath.get.toFile
        IO.delete(dir)
        dir.mkdir
        val args: Array[String] = Array(
          options,
          Array("-d", dir.getAbsolutePath),
          Array(
            "-classpath",
            classpath
              .map(c => converter.toPath(c).toAbsolutePath.toString)
              .mkString(File.pathSeparator)
          ),
          source.map(converter.toPath(_).toFile.getAbsolutePath)
        ).flatten
        val s = state.value
        val e = Project.extract(s)
        e.runInputTask(
          forkCompilerProject / Compile / run,
          args.mkString(" ", " ", ""),
          s
        )
      }

      override def scalaInstance =
        Keys.scalaInstance.value
    }
  )
}

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    commonSettings,
    name := UpdateReadme.scalapbJsonCommonName,
    description := "Json/Protobuf convertors for ScalaPB",
    (Compile / packageSrc / mappings) ++= (Compile / managedSources).value.map { f =>
      // https://github.com/sbt/sbt-buildinfo/blob/v0.7.0/src/main/scala/sbtbuildinfo/BuildInfoPlugin.scala#L58
      val buildInfoDir = "sbt-buildinfo"
      val path = if (f.getAbsolutePath.contains(buildInfoDir)) {
        (file(buildInfoPackage.value) / f
          .relativeTo((Compile / sourceManaged).value / buildInfoDir)
          .get
          .getPath).getPath
      } else {
        f.relativeTo((Compile / sourceManaged).value).get.getPath
      }
      (f, path)
    },
    buildInfoPackage := "scalapb_json",
    buildInfoObject := "ScalapbJsonCommonBuildInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      "scalapbVersion" -> scalapbV,
      scalaVersion,
      version
    )
  )
  .jvmSettings(
    (Test / PB.targets) := Seq(
      PB.gens.java -> (Test / sourceManaged).value,
      scalapb.gen(javaConversions = true) -> (Test / sourceManaged).value
    ),
    compileOrder := {
      if (isScala3.value) {
        // https://github.com/lampepfl/dotty/issues/10956
        // https://github.com/lampepfl/dotty/issues/6138
        CompileOrder.JavaThenScala
      } else {
        compileOrder.value
      }
    },
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java-util" % "3.19.4" % "test"
    )
  )
  .jsSettings(
    scalacOptions += {
      val a = (LocalRootProject / baseDirectory).value.toURI.toString
      val g =
        "https://raw.githubusercontent.com/scalapb-json/scalapb-json-common/" + tagOrHash.value
      if (isScala3.value) {
        "-scalajs-mapSourceURI:$a->$g/"
      } else {
        "-P:scalajs:mapSourceURI:$a->$g/"
      }
    },
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.3.0",
    ),
  )
  .platformsSettings(JVMPlatform, JSPlatform)(
    Seq(Compile, Test).map { x =>
      x / unmanagedSourceDirectories += {
        baseDirectory.value.getParentFile / "jvm-js" / "src" / Defaults.nameForSrc(
          x.name
        ) / "scala",
      }
    },
  )
  .platformsSettings(JSPlatform, NativePlatform)(
    Seq(Compile, Test).map { x =>
      x / unmanagedSourceDirectories += {
        baseDirectory.value.getParentFile / "js-native" / "src" / Defaults.nameForSrc(
          x.name
        ) / "scala",
      }
    },
    (Test / PB.targets) := Seq(
      scalapb.gen(javaConversions = false) -> (Test / sourceManaged).value
    ),
  )
  .settings(
    scalapropsCoreSettings,
    libraryDependencies += "com.github.scalaprops" %%% "scalaprops" % "0.9.0" % "test",
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVersion % "test",
  )
  .nativeSettings(
    disableScala3
  )

lazy val macros = project.settings(
  commonSettings,
  description := "Json/Protobuf convertor macros for ScalaPB",
  name := UpdateReadme.scalapbJsonMacrosName,
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % scalatestVersion % "test",
  ),
  libraryDependencies ++= {
    if (isScala3.value) {
      Nil
    } else {
      Seq(
        scalaOrganization.value % "scala-reflect" % scalaVersion.value,
      )
    }
  },
)

lazy val macrosJava = project
  .settings(
    commonSettings,
    name := UpdateReadme.scalapbJsonMacrosJavaName,
    description := "Json/Protobuf convertor macros for ScalaPB with protobuf-java-util dependency",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test",
      "com.google.protobuf" % "protobuf-java-util" % protobufVersion,
    ),
    disableScala3,
  )
  .dependsOn(
    macros,
    coreJVM % "test->test",
  )

lazy val tests = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    commonSettings,
    compilers := forkScalaCompiler.value,
    Compile / mainClass := Some("scalapb_json.ProtoMacrosTest"),
    noPublish,
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVersion,
  )
  .jsSettings(
    Compile / scalaJSUseMainModuleInitializer := true,
  )
  .nativeSettings(
    disableScala3
  )
  .configure(_ dependsOn (macros, macrosJava))

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js

commonSettings

lazy val noPublish = Seq(
  PgpKeys.publishLocalSigned := {},
  PgpKeys.publishSigned := {},
  publishLocal := {},
  publish := {},
  Compile / publishArtifact := false
)

noPublish

lazy val commonSettings = Def.settings(
  scalapbV := scalapbVersion,
  (Compile / unmanagedResources) += (LocalRootProject / baseDirectory).value / "LICENSE.txt",
  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala212, Scala213, Scala3),
  scalacOptions ++= {
    if (isScala3.value) {
      Nil
    } else {
      unusedWarnings
    }
  },
  Seq(Compile, Test).flatMap(c => c / console / scalacOptions --= unusedWarnings),
  scalacOptions ++= Seq("-feature", "-deprecation", "-language:existentials"),
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  organization := "io.github.scalapb-json",
  Project.inConfig(Test)(sbtprotoc.ProtocPlugin.protobufConfigSettings),
  Compile / PB.targets := Nil,
  (Test / PB.protoSources) := Seq(baseDirectory.value.getParentFile / "shared/src/test/protobuf"),
  libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapbV.value,
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbV.value % "protobuf,test",
    "com.lihaoyi" %%% "utest" % "0.7.11" % "test",
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  (Global / pomExtra) := {
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
  publishTo := sonatypePublishToBundle.value,
  (Compile / doc / scalacOptions) ++= {
    val t = tagOrHash.value
    Seq(
      "-sourcepath",
      (LocalRootProject / baseDirectory).value.getAbsolutePath,
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
        extracted
          .runAggregated(extracted.get(thisProjectRef) / (Global / PgpKeys.publishSigned), state)
      },
      enableCrossBuild = true
    ),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    UpdateReadme.updateReadmeProcess,
    pushChanges
  )
)

val coreJVM = core.jvm
val coreJS = core.js

commonSettings
publishArtifact := false
publish := {}
publishLocal := {}
PgpKeys.publishSigned := {}
PgpKeys.publishLocalSigned := {}
