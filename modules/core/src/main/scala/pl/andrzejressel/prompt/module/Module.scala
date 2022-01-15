package pl.andrzejressel.prompt.module

import cats.effect.IO
import io.odin.Logger
import pl.andrzejressel.prompt.model.{
  Color,
  ConsoleState,
  LoggerComponent,
  Segment
}

trait Module {

  def getModulePipe: fs2.Pipe[IO, ConsoleState, Option[Segment]]

  protected def textColor: Color
  protected def backgroundColor: Color

  protected val logger: Logger[IO] = LoggerComponent.logger[IO]

}
