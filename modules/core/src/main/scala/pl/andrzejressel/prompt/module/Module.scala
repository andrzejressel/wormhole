package pl.andrzejressel.prompt.module

import cats.effect.Concurrent
import cats.effect.kernel.Sync
import io.odin.Logger
import pl.andrzejressel.prompt.model.{ConsoleState, LoggerComponent, Segment}

abstract class Module[F[_]: Sync: Concurrent] {

  def getModulePipe: fs2.Pipe[F, ConsoleState, Option[Segment]]

  protected val logger: Logger[F] = LoggerComponent.logger[F]

}
