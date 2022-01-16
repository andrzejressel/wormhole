package pl.andrzejressel.prompt.module

import cats.effect.IO
import cats.effect.cps._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.catsSyntaxOptionId
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import pl.andrzejressel.prompt.model.AnsiColor.Black
import pl.andrzejressel.prompt.model.{ConsoleState, Segment}
import pl.andrzejressel.prompt.utils.PromptEventually

import java.util.concurrent.atomic.AtomicInteger

class FunctionModuleSpec
    extends AsyncFlatSpec
    with should.Matchers
    with AsyncIOSpec
    with PromptEventually {

  it should "handle duplicates at input" in {

    val createSegmentInvocations = new AtomicInteger(0)

    val cs1 = ConsoleState(pid = 1.some, currentDirectory = None, env = Map())
    val cs2 = ConsoleState(pid = 2.some, currentDirectory = None, env = Map())
    val cs3 = ConsoleState(pid = 3.some, currentDirectory = None, env = Map())

    val csStream = fs2.Stream(cs1, cs1, cs2, cs2, cs2, cs3)

    val module = new FunctionModule {
      override def createSegment(state: ConsoleState): Option[Segment] = {
        createSegmentInvocations.incrementAndGet()
        None
      }
    }

    for {
      _ <- csStream.through(module.getModulePipe).compile.drain
      _ <- IO(createSegmentInvocations).asserting(_.get() shouldBe 3)
    } yield ()
  }

  it should "handle duplicates at output" in async[IO] {
    val createSegmentInvocations = new AtomicInteger(0)

    val csEven1 =
      ConsoleState(pid = 1.some, currentDirectory = None, env = Map())
    val csEven2 =
      ConsoleState(pid = 3.some, currentDirectory = None, env = Map())
    val csEven3 =
      ConsoleState(pid = 5.some, currentDirectory = None, env = Map())

    val csOdd1 =
      ConsoleState(pid = 2.some, currentDirectory = None, env = Map())
    val csOdd2 =
      ConsoleState(pid = 4.some, currentDirectory = None, env = Map())

    val csStream = fs2.Stream(csEven1, csEven2, csOdd1, csOdd2, csEven3)

    val module = new FunctionModule {
      override def createSegment(state: ConsoleState): Option[Segment] = {
        createSegmentInvocations.incrementAndGet()
        Segment((state.pid.get % 2).toString, Black, Black).some
      }
    }

    val segments = csStream
      .through(module.getModulePipe)
      .compile
      .toList
      .await

    segments shouldEqual Seq(
      Segment("1", Black, Black).some,
      Segment("0", Black, Black).some,
      Segment("1", Black, Black).some
    )

    createSegmentInvocations.get() shouldBe 5

  }

}
