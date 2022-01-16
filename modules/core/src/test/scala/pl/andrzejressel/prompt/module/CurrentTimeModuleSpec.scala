package pl.andrzejressel.prompt.module

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import pl.andrzejressel.prompt.model.AnsiColor.Black
import pl.andrzejressel.prompt.model.ConsoleState
import pl.andrzejressel.prompt.utils.PromptEventually

import java.time.{Duration, Instant}

class CurrentTimeModuleSpec
    extends AsyncFlatSpec
    with should.Matchers
    with AsyncIOSpec
    with PromptEventually {

  it should "generate full seconds" in {

    val stream =
      fs2.Stream(ConsoleState.initial)

    val module = CurrentTimeModule(Black, Black)

    for {
      startTime <- IO(Instant.now())
      values    <- stream
                     .through(module.getModulePipe)
                     .take(5)
                     .compile
                     .toList

      _ = values shouldNot contain(None)
      _ = values should have size 5
      // List contain distinct elements
      _ = values.toSet should have size 5

      endTime <- IO(Instant.now())
      _       <- IO(Duration.between(startTime, endTime))
                   .map(_.toSeconds)
                   .asserting(Seq(4, 5, 6) should contain(_))
    } yield ()

  }

}
