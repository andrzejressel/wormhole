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
    "-language:implicitConversions"
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
  .aggregate(core, dsl, macros, resourceTest)
  .settings(
    name := "prompt"
  )

lazy val core = (project in file("modules/core"))
  .enablePlugins(NativeImagePlugin)
  .dependsOn(macros)
  .settings(
    commonSettings,
    nativeImageSettings,
    name      := "core",
    libraryDependencies ++= Dependencies.all,
    mainClass := Some("pl.andrzejressel.prompt.Main")
  )

lazy val dsl = (project in file("modules/dsl"))
  .dependsOn(core)
  .settings(
    commonSettings,
    name := "dsl"
  )

lazy val macros = (project in file("modules/macros"))
  .settings(
    commonSettings,
    libraryDependencies += scalaVersion(
      "org.scala-lang" % "scala-reflect" % _
    ).value,
    name := "macros"
  )

lazy val resourceTest = (project in file("modules/resource_test"))
  .enablePlugins(NativeImagePlugin)
  .dependsOn(core)
  .settings(
    commonSettings,
    nativeImageSettings,
    name      := "resource_test",
    mainClass := Some("pl.andrzejressel.prompt.resource_test.Main")
  )
