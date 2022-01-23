import sbt.Keys.mainClass

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / excludeLintKeys += nativeImageVersion
Global / excludeLintKeys += mainClass

scalaVersion := Dependencies.scalaVersion

val commonSettings = Seq(
  version           := "0.1.0-SNAPSHOT",
  // Locked by https://github.com/circe/circe-generic-extras/issues/168
  scalaVersion      := Dependencies.scalaVersion,   // 2.11.12, 2.13.7, or 3.x
  semanticdbEnabled := true,                        // enable SemanticDB
  semanticdbVersion := scalafixSemanticdb.revision, // only required for Scala 2.x
  scalacOptions     := Seq(
    "-encoding",
    "utf8",
    "-deprecation",
    "-unchecked",
    "-Xlint",
    "-Xfatal-warnings",
    "-Xlint:-byname-implicit",
    // "-feature",
    "-language:implicitConversions",
    "-Xasync"
    // ...
  ),
  resolvers +=
    "Jetbrains repository" at "https://packages.jetbrains.team/maven/p/ij/intellij-dependencies"
)

val nativeImageSettings = Seq(
  nativeImageOptions ++= List(
    "--no-fallback"
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
