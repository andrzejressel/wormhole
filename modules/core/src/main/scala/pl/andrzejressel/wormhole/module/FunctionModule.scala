package pl.andrzejressel.wormhole.module

import cats.effect.IO
import cats.implicits._
import fs2.Pipe
import pl.andrzejressel.wormhole.model.{ConsoleState, Segment}

trait FunctionModule extends Module {

  def createSegment(state: ConsoleState): Option[Segment]

  final override def getModulePipe: Pipe[IO, ConsoleState, Option[Segment]] =
    _.changes
      .map(createSegment)
      .changes

}
