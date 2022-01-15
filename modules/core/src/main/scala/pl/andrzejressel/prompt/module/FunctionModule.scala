package pl.andrzejressel.prompt.module

import cats.effect.IO
import cats.implicits._
import fs2.Pipe
import pl.andrzejressel.prompt.model.{ConsoleState, Segment}

trait FunctionModule extends Module {

  def createSegment(state: ConsoleState): Option[Segment]

  final override def getModulePipe: Pipe[IO, ConsoleState, Option[Segment]] = {
    stream =>
      stream.changes
        .map(createSegment)
        .changes
  }

}
