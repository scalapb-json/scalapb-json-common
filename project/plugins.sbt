addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.2.6")

addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.3.8")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.25")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.0")

addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.9")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.18")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.2"
