package pl.andrzejressel.wormhole

import cats.effect.IO
import cats.effect.cps.{AwaitSyntax, async}
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.concurrent.Eventually
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import pl.andrzejressel.wormhole.model.SetEnvironment

import java.nio.file.Files
import scala.concurrent.duration.DurationInt

class ConsoleEventsReaderTest
    extends AsyncFlatSpec
    with should.Matchers
    with AsyncIOSpec
    with Eventually {

  it should "should read already existing files" in async[IO] {

    val dir = Files.createTempDirectory(null)
    Files.writeString(
      dir.resolve("file1.json"),
      """{"type": "set_environment", "env": {}}"""
    )

    val reader = ConsoleEventsReader[IO](dir)

    val list = reader
      .readConsoleEvents()
      .take(1)
      .compile
      .toList
      .timeoutAndForget(5.seconds)
      .await

    list should contain only SetEnvironment(Map())

  }

}
