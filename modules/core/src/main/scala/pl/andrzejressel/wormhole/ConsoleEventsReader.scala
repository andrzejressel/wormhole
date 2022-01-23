package pl.andrzejressel.wormhole

import cats.effect.{Async, Concurrent}
import fs2.io.file.Files
import fs2.io.file.Path.fromNioPath
import fs2.io.file.Watcher.{Event, EventType}
import pl.andrzejressel.wormhole.model.ConsoleEvent
import pl.andrzejressel.wormhole.unsafe.File.readFile
import pl.andrzejressel.wormhole.unsafe.JSON.decode
import pl.andrzejressel.wormhole.interop.FS2._

import java.nio.file.{Path => JPath}
import scala.concurrent.duration.DurationInt

case class ConsoleEventsReader[
  F[_]: Concurrent: Async: Files
](file: JPath) {

  private val fs2Path = fromNioPath(file)

  def readConsoleEvents(): fs2.Stream[F, ConsoleEvent] = {
    Files[F]
      .watch(fs2Path, Seq(EventType.Modified), Nil, 1.second)
      .flatMap(pathOf)
      .flatMap(readFile[F])
      .flatMap(decode[F, ConsoleEvent])
  }

  def pathOf(event: Event): Option[JPath] =
    event match {
      case Event.Created(p, _)                       => Some(p.toNioPath)
      case Event.Deleted(p, _)                       => Some(p.toNioPath)
      case Event.Modified(p, _)                      => Some(p.toNioPath)
      case Event.Overflow(_)                         => None
      case Event.NonStandard(e, registeredDirectory) =>
        e.context match {
          case path: JPath =>
            Some(registeredDirectory.toNioPath.resolve(path))
          case _           => None
        }
    }

}
