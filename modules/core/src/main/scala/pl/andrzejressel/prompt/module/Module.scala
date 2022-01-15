package pl.andrzejressel.prompt.module

import cats.effect.IO
import io.odin.Logger
import pl.andrzejressel.prompt.model.{ConsoleState, LoggerComponent, Segment}

trait Module {
  def getModulePipe: fs2.Pipe[IO, ConsoleState, Option[Segment]]
  protected val logger: Logger[IO] = LoggerComponent.logger[IO]
}
