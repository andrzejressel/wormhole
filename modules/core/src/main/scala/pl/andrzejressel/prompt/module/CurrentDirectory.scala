package pl.andrzejressel.prompt.module

import cats.effect.Async
import fs2.Pipe
import pl.andrzejressel.prompt.interop.Java.eqPath
import pl.andrzejressel.prompt.model.{AnsiColor, ConsoleState, Segment}

case class CurrentDirectory[F[_]: Async]() extends Module[F] {

  override def getModulePipe: Pipe[F, ConsoleState, Option[Segment]] = {
    stream =>
      stream
        .map(_.currentDirectory)
        .unNone
        .changes
        .evalTap(path => logger.debug(s"New path $path"))
        .map(currentDirectory =>
          Segment(currentDirectory.toString, AnsiColor.White, AnsiColor.Black)
        )
        .map(Some(_))
  }

}
