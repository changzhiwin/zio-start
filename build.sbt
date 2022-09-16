scalaVersion := "2.13.8"
organization := "zio.reference.experiment"
name := "practice"

val ZIOVersion = "2.0.0"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % ZIOVersion, 
  "dev.zio" %% "zio-streams" % ZIOVersion, 
  "dev.zio" %% "zio-macros" % ZIOVersion
)

// Enable macro expansion, 
// from https://zio.dev/reference/service-pattern/generating-accessor-methods-using-macros
scalacOptions += "-Ymacro-annotations"