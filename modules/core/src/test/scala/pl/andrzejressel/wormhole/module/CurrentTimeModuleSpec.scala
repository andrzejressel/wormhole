package pl.andrzejressel.wormhole.module

import cats.effect.IO
import cats.effect.cps._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import pl.andrzejressel.wormhole.model.AnsiColor.Black
import pl.andrzejressel.wormhole.model.ConsoleState
import pl.andrzejressel.wormhole.test_utils.WormholeEventually

import java.time.{Duration, Instant}

class CurrentTimeModuleSpec
    extends AsyncFlatSpec
    with should.Matchers
    with AsyncIOSpec
    with WormholeEventually {

  it should "generate full seconds" in eventually {
    async[IO] {
      val stream =
        fs2.Stream(ConsoleState.initial)

      val module = CurrentTimeModule(Black, Black)

      val startTime = Instant.now()
      val values    = stream
        .through(module.getModulePipe)
        .take(5)
        .compile
        .toList
        .await

      values shouldNot contain(None)
      values should have size 5
      // List contain distinct elements
      values.toSet should have size 5

      val endTime           = Instant.now()
      val durationInSeconds = Duration.between(startTime, endTime).toSeconds
      Seq(4, 5, 6) should contain(durationInSeconds)
    }
  }

}
