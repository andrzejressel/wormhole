package pl.andrzejressel.prompt.utils

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.Pipe
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should
import pl.andrzejressel.prompt.utils.FS2Utils.{
  prefetchKeepLatest,
  shareAndCombine
}

import scala.concurrent.duration.DurationInt

class FS2UtilsSpec extends AsyncFreeSpec with should.Matchers with AsyncIOSpec {

  "prefetchKeepLatest" - {
    "should drop buffered elements" in {
      fs2
        .Stream[IO, Int](1, 2, 3, 4)
        .evalMap(i => IO.sleep(30.milliseconds) >> IO(i))
        .through(prefetchKeepLatest())
        .evalMap(i => IO.sleep(80.milliseconds) >> IO(i))
        .compile
        .toList
        .asserting(_ should contain allElementsOf Seq(1, 3))
    }
  }

  "shareAndCombine" - {
    "fast pipes does not wait for slow ones" in {
      def fastPipe[I]: Pipe[IO, I, Option[I]] =
        src => src.evalMap(a => IO.sleep(30.milliseconds) >> IO(Some(a)))

      def slowPipe[I]: Pipe[IO, I, Option[I]] =
        src => src.evalMap(a => IO.sleep(80.milliseconds) >> IO(Some(a)))

      val expected = List(
        List(None, None),
        List(Some(1), None),
        List(Some(2), None),
        List(Some(2), Some(1)),
        List(Some(3), Some(1)),
        List(Some(3), Some(2)),
        List(Some(3), Some(3))
      )

      fs2
        .Stream[IO, Int](1, 2, 3)
        .through(shareAndCombine(Seq(fastPipe[Int], slowPipe[Int])))
        .compile
        .toList
        .asserting(_ should equal(expected))

    }
  }

}
