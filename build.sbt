scalaVersion := "2.13.8"
organization := "zio.reference.experiment"
name := "practice"

val ZIOVersion = "2.0.2"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % ZIOVersion, 
  "dev.zio" %% "zio-streams" % ZIOVersion, 
  "dev.zio" %% "zio-macros" % ZIOVersion,
  "dev.zio" %% "zio-test"          % ZIOVersion % Test,
  "dev.zio" %% "zio-test-sbt"      % ZIOVersion % Test,
  "dev.zio" %% "zio-test-magnolia" % ZIOVersion % Test
)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

// Enable macro expansion, 
// from https://zio.dev/reference/service-pattern/generating-accessor-methods-using-macros
scalacOptions += "-Ymacro-annotations"