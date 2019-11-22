name := "zio_study"
version := "1.0"
scalaVersion := "2.12.8"

lazy val Versions = new {
  val phantom = "2.42.0"
  val slick = "3.3.2"
}

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.typesafeRepo("releases"),
  Resolver.bintrayRepo("websudos", "oss-releases")
)

libraryDependencies += "com.datastax.oss" % "java-driver-core" % "4.0.1"
libraryDependencies += "com.typesafe" % "config" % "1.3.4"
libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC17"

assemblyMergeStrategy in assembly := {
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in (Compile, run) := Some("pkg.FormCalculatorApp")

assemblyJarName in assembly :="FormCalculator.jar"
mainClass in (Compile, packageBin) := Some("pkg.FormCalculatorApp")
mainClass in (Compile, run) := Some("pkg.FormCalculatorApp")

