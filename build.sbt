name := "ZioStudy"

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

libraryDependencies += "com.typesafe" % "config" % "1.3.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.3.0-alpha4"
libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC16"

/*
assemblyJarName in assembly :="ZioStudy.jar"
mainClass in (Compile, packageBin) := Some("pkg.ZioStudy")
*/
mainClass in (Compile, run) := Some("pkg.MyApp")

