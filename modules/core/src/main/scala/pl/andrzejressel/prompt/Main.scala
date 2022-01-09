package pl.andrzejressel.prompt

import cats.effect._
import cats.effect.kernel.Async
import cats.implicits._
import fs2.io.file.Files.forAsync
import fs2.io.file._
import io.odin.consoleLogger
import pl.andrzejressel.prompt.ColorToShellConverter.{
  RESET_COLOR,
  getBackgroundEscapeCode,
  getForegroundEscapeCode
}
import pl.andrzejressel.prompt.model.{ConsoleReducer, ConsoleState, Segment}
import pl.andrzejressel.prompt.module.{CurrentDirectory, CurrentTimeModule}
import pl.andrzejressel.prompt.terminal.Terminal
import pl.andrzejressel.prompt.terminal.Terminal.PowerShell
import pl.andrzejressel.prompt.unsafe.File.writeToFile
import pl.andrzejressel.prompt.utils.FS2Utils
import pl.andrzejressel.prompt.utils.FS2Utils.prefetchKeepLatest

import java.nio.file.Paths
import java.util.Base64

object Main extends IOApp.Simple {

  override def run: IO[Unit] = run0[IO]()

  def run0[F[_]: Concurrent: Async: Files](): F[Unit] = {

    val logger = consoleLogger()

    val terminal = PowerShell

    val inputFile  = Paths.get("D:\\events")
    val outputFile = Paths.get("D:\\shell.txt")

    val fileReader = ConsoleEventsReader[F](inputFile)

    val modules = Seq(
      CurrentDirectory[F](),
      CurrentTimeModule[F]()
    )
    val pipes   = modules.map(prefetchKeepLatest() andThen _.getModulePipe)

    fileReader
      .readConsoleEvents()
      .scan(ConsoleState.initial)(ConsoleReducer)
      .changes
      .evalTap(state => logger.debug(f"Console state: ${state.show}"))
      .through(FS2Utils.shareAndCombine(pipes))
      .map(_.flatten)
      .map(toPrompt(_, terminal))
      .map(text => Base64.getEncoder.encodeToString(text.getBytes()))
      .evalMap(writeToFile[F](outputFile))
      .compile
      .drain

  }

  def toPrompt(segments: Seq[Segment], terminal: Terminal): String = {

    segments.map { segment =>
      val bg = getBackgroundEscapeCode(segment.backgroundColor)
      val fg = getForegroundEscapeCode(segment.textColor)

      terminal.escapeColor(bg + fg) + segment.text + terminal.escapeColor(
        RESET_COLOR
      )
    }.mkString + "\n\ue0b0 "

  }

}
