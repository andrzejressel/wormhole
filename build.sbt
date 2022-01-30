import sbt.Keys.mainClass

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / excludeLintKeys += nativeImageVersion
Global / excludeLintKeys += mainClass

scalaVersion := Dependencies.scalaVersion

val windowsOnlyTag = "tags.WindowsOnly"
val linuxOnlyTag   = "tags.LinuxOnly"
val macOnlyTag     = "tags.MacOnly"

val tagsToIgnore = {
  System.getProperty("os.name").toLowerCase match {
    case mac if mac.contains("mac")       =>
      List(windowsOnlyTag, linuxOnlyTag)
    case win if win.contains("win")       =>
      List(linuxOnlyTag, macOnlyTag)
    case linux if linux.contains("linux") =>
      List(windowsOnlyTag, macOnlyTag)
    case osName                           =>
      throw new RuntimeException(s"Unknown operating system $osName")
  }
}.map(tag =>
  Tests.Argument(
    TestFrameworks.ScalaTest,
    "-l",
    tag
  )
)

val commonSettings = Seq(
  version           := "0.1.0-SNAPSHOT",
  // Locked by https://github.com/circe/circe-generic-extras/issues/168
  scalaVersion      := Dependencies.scalaVersion,
  semanticdbEnabled := true,                        // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision, // only required for Scala 2.x
  scalacOptions     := Seq(
    "-encoding",
    "utf8",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings",
    "-language:implicitConversions"
  ),
  resolvers +=
    "Jetbrains repository" at "https://packages.jetbrains.team/maven/p/ij/intellij-dependencies",
  Test / testOptions ++= tagsToIgnore
)

val nativeImageSettings = Seq(
  nativeImageOptions ++= List(
    "--no-fallback",
    "-J-Dfile.encoding=UTF-8"
  ),
  nativeImageVersion := "21.3.0"
)

lazy val root = (project in file("."))
  .aggregate(core, example, resourceTest)
  .settings(
    name := "wormhole"
  )

lazy val core = (project in file("modules/core"))
  .enablePlugins(NativeImagePlugin)
  .dependsOn(testUtils % Test)
  .settings(
    commonSettings,
    name := "core",
    libraryDependencies ++= Dependencies.all
  )

lazy val example = (project in file("modules/example"))
  .enablePlugins(NativeImagePlugin)
  .dependsOn(core)
  .settings(
    commonSettings,
    nativeImageSettings,
    name      := "example",
    mainClass := Some("pl.andrzejressel.wormhole.example.Main")
  )

lazy val resourceTest = (project in file("modules/resource_test"))
  .enablePlugins(NativeImagePlugin)
  .dependsOn(core)
  .settings(
    commonSettings,
    nativeImageSettings,
    name      := "resource_test",
    mainClass := Some("pl.andrzejressel.wormhole.resource_test.Main")
  )

lazy val testUtils = (project in file("modules/test_utils"))
  .settings(
    commonSettings,
    name := "test_utils",
    libraryDependencies ++= Dependencies.all.map(
      _.withConfigurations(None) % Compile
    )
  )

lazy val e2e = (project in file("modules/e2e"))
  .dependsOn(testUtils % Test)
  .settings(
    commonSettings,
    name := "e2e"
  )
