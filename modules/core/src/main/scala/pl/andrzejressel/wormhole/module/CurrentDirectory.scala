package pl.andrzejressel.wormhole.module

import cats.effect.IO
import fs2.Pipe
import pl.andrzejressel.wormhole.interop.Java.eqPath
import pl.andrzejressel.wormhole.model.{Color, ConsoleState, Segment}

case class CurrentDirectory(
  textColor: Color,
  backgroundColor: Color
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
