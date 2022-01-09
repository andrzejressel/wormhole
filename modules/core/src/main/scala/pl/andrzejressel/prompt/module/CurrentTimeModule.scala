package pl.andrzejressel.prompt.module

import cats.effect.kernel.Sync
import cron4s.Cron
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Pipe
import pl.andrzejressel.prompt.model.{AnsiColor, ConsoleState, Segment}

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

case class CurrentTimeModule[F[_]: cats.effect.Temporal: Sync](
  pattern: String = "yyyy-MM-dd HH:mm:ss"
) extends Module[F] {

  private val SYNC          = Sync[F]
  private val cronScheduler = Cron4sScheduler.systemDefault[F]
  private val evenSeconds   = Cron.unsafeParse("* * * ? * *")

  private val DATE_TIME_FORMATTER = DateTimeFormatter
    .ofPattern(pattern)
    .withZone(ZoneId.systemDefault())

  override def getModulePipe: Pipe[F, ConsoleState, Option[Segment]] = {
    stream =>
      cronScheduler
        .awakeEvery(evenSeconds)
        .concurrently(stream)
        .evalMap(_ => SYNC.delay(Instant.now()))
        .map(DATE_TIME_FORMATTER.format)
        .map(i => Segment(i, AnsiColor.Red.bright(), AnsiColor.Black))
        .map(Some(_))
  }

}
