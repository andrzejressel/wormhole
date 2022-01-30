package pl.andrzejressel.wormhole.module

import cats.effect.IO
import cats.effect.cps._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.catsSyntaxOptionId
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.typelevel.ci.CIStringSyntax
import pl.andrzejressel.wormhole.model.AnsiColor.Black
import pl.andrzejressel.wormhole.model.{ConsoleState, Segment}

import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

class FunctionModuleSpec
    extends AsyncFlatSpec
    with should.Matchers
    with AsyncIOSpec {

  it should "handle duplicates at input" in async[IO] {

    val createSegmentInvocations = new AtomicInteger(0)

    val cs1 = ConsoleState(currentDirectory = None, env = Map(ci"a" -> "1"))
    val cs2 = ConsoleState(currentDirectory = None, env = Map(ci"a" -> "2"))
    val cs3 = ConsoleState(currentDirectory = None, env = Map(ci"a" -> "3"))

    val csStream = fs2.Stream(cs1, cs1, cs2, cs2, cs2, cs3)

    val module = new FunctionModule {
      override def createSegment(state: ConsoleState): Option[Segment] = {
        createSegmentInvocations.incrementAndGet()
        None
      }
    }

    csStream.through(module.getModulePipe).compile.drain.await
    createSegmentInvocations.get() shouldBe 3
  }

  it should "handle duplicates at output" in async[IO] {
    val createSegmentInvocations = new AtomicInteger(0)
    val path                     = Paths.get("")

    val csWithPath1 =
      ConsoleState(currentDirectory = path.some, env = Map(ci"a" -> "1"))
    val csWithPath2 =
      ConsoleState(currentDirectory = path.some, env = Map(ci"a" -> "2"))
    val csWithPath3 =
      ConsoleState(currentDirectory = path.some, env = Map(ci"a" -> "3"))

    val csWithoutPath1 =
      ConsoleState(currentDirectory = None, env = Map(ci"a" -> "4"))
    val csWithoutPath2 =
      ConsoleState(currentDirectory = None, env = Map(ci"a" -> "5"))

    val csStream =
      fs2.Stream(
        csWithPath1,
        csWithPath2,
        csWithoutPath1,
        csWithoutPath2,
        csWithPath3
      )

    val module = new FunctionModule {
      override def createSegment(state: ConsoleState): Option[Segment] = {
        createSegmentInvocations.incrementAndGet()
        Segment(state.currentDirectory.toString, Black, Black).some
      }
    }

    val segments = csStream
      .through(module.getModulePipe)
      .compile
      .toList
      .await

    segments shouldEqual Seq(
      Segment(path.some.toString, Black, Black).some,
      Segment(None.toString, Black, Black).some,
      Segment(path.some.toString, Black, Black).some
    )

    createSegmentInvocations.get() shouldBe 5

  }

}
