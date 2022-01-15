import sbt._

object Dependencies {

  private val fs2Version        = "3.2.0"
  private val circeVersion      = "0.14.1"
  private val circeFs2Version   = "0.14.0"
  private val catsEffectVersion = "3.3.0"
  private val enumeratumVersion = "1.7.0"
  private val refinedVersion    = "0.9.28"

  private object Cats {
    private val catsEffect =
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    private val kittens = "org.typelevel" %% "kittens" % "2.3.2"

    val all = Seq(catsEffect, kittens)
  }

  private object FS2 {
    private val main = Seq(
      "core",
      "io",
      "reactive-streams",
      "scodec"
    ).map(d => "co.fs2" %% f"fs2-$d" % fs2Version)

    private val cron = "eu.timepit" %% "fs2-cron-cron4s" % "0.7.1"

    val all: Seq[ModuleID] = main :+ cron
  }

  private object Circe {
    private val main     = Seq(
      "core",
      "generic",
      "generic-extras",
      "parser"
    ).map(d => "io.circe" %% f"circe-$d" % circeVersion)
    private val circeFs2 = "io.circe" %% "circe-fs2" % circeFs2Version

    val all: Seq[ModuleID] = main :+ circeFs2
  }

  private val enumeratum = "com.beachape" %% "enumeratum" % enumeratumVersion

  private object Refined {
    private val core       = "eu.timepit" %% "refined" % refinedVersion
    private val scalacheck =
      "eu.timepit" %% "refined-scalacheck" % refinedVersion
    private val cats = "eu.timepit" %% "refined-cats" % refinedVersion

    val all = Seq(core, cats, scalacheck)
  }

  private object ApacheCommons {
    private val lang3 = "org.apache.commons" % "commons-lang3" % "3.12.0"

    val all = Seq(lang3)
  }

  private object ScalaTest {
    val scalactic          = "org.scalactic"  %% "scalactic"  % "3.2.10" % Test
    val scalaTest          = "org.scalatest"  %% "scalatest"  % "3.2.10" % Test
    val scalaCheck         = "org.scalacheck" %% "scalacheck" % "1.15.4" % Test
    val scalatestPlusCheck =
      "org.scalatestplus" %% "scalacheck-1-15" % "3.2.10.0" % Test
    val catsEffect =
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test

    val log4JApi  = "org.apache.logging.log4j" % "log4j-api"  % "2.17.1" % Test
    val log4JCore = "org.apache.logging.log4j" % "log4j-core" % "2.17.1" % Test

    val all =
      Seq(
        scalactic,
        scalaTest,
        scalaCheck,
        scalatestPlusCheck,
        catsEffect,
        log4JApi,
        log4JCore
      )
  }

  private val odin = Seq(
    "com.github.valskalla" %% "odin-core",
    "com.github.valskalla" %% "odin-json",  // to enable JSON formatter if needed
    "com.github.valskalla" %% "odin-extras" // to enable additional features if needed (see docs)
  ).map(_ % "0.13.0")

  private val caseInsensitive = "org.typelevel"      %% "case-insensitive" % "1.2.0"
  private val pty4j           = "org.jetbrains.pty4j" % "pty4j"            % "0.12.6" % Test

  val scalaVersion       = "2.13.7"
  val all: Seq[ModuleID] =
    Seq(enumeratum, caseInsensitive, pty4j) ++
      FS2.all ++
      Cats.all ++
      Circe.all ++
      Refined.all ++
      ApacheCommons.all ++
      ScalaTest.all ++
      odin

}
