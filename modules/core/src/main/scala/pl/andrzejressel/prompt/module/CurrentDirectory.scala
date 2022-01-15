package pl.andrzejressel.prompt.module

import cats.effect.IO
import fs2.Pipe
import pl.andrzejressel.prompt.interop.Java.eqPath
import pl.andrzejressel.prompt.model.{Color, ConsoleState, Segment}

case class CurrentDirectory(
  override val textColor: Color,
  override val backgroundColor: Color
) extends Module {

  override def getModulePipe: Pipe[IO, ConsoleState, Option[Segment]] = {
    stream =>
      stream
        .map(_.currentDirectory)
        .unNone
        .changes
        .evalTap(path => logger.debug(s"New path $path"))
        .map(path => Segment(path.toString, textColor, backgroundColor))
        .map(Some(_))
  }

}
