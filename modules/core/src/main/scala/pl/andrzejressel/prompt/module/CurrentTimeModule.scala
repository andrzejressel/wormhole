package pl.andrzejressel.prompt.module

import cats.effect.IO
import cats.effect.kernel.Sync
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Pipe
import pl.andrzejressel.prompt.model.{Color, ConsoleState, Segment}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

case class CurrentTimeModule(
  textColor: Color,
  backgroundColor: Color,
  pattern: String = "yyyy-MM-dd HH:mm:ss"
) extends Module {

  private val SYNC          = Sync[IO]
  private val cronScheduler = Cron4sScheduler.systemDefault[IO]
  private val evenSeconds   = Cron.unsafeParse("* * * ? * *")

  private val DATE_TIME_FORMATTER = DateTimeFormatter
    .ofPattern(pattern)
    .withZone(ZoneId.systemDefault())

  override def getModulePipe: Pipe[IO, ConsoleState, Option[Segment]] = {
    stream =>
      cronScheduler
        .awakeEvery(evenSeconds)
        .concurrently(stream)
        .evalMap(_ => SYNC.delay(Instant.now()))
        .map(DATE_TIME_FORMATTER.format)
        .map(Segment(_, textColor, backgroundColor))
        .map(Some(_))
  }

}
