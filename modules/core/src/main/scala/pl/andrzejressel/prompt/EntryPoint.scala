package pl.andrzejressel.prompt

import cats.effect._
import cats.implicits._
import fs2.io.file.Files.forAsync
import io.odin.consoleLogger
import pl.andrzejressel.prompt.ColorToShellConverter.{
  RESET_COLOR,
  getBackgroundEscapeCode,
  getForegroundEscapeCode
}
import pl.andrzejressel.prompt.model.{ConsoleReducer, ConsoleState, Segment}
import pl.andrzejressel.prompt.module.Module
import pl.andrzejressel.prompt.terminal.Terminal
import pl.andrzejressel.prompt.terminal.Terminal.PowerShell
import pl.andrzejressel.prompt.unsafe.File.writeToFile
import pl.andrzejressel.prompt.utils.FS2Utils
import pl.andrzejressel.prompt.utils.FS2Utils.prefetchKeepLatest

import java.nio.file.Paths
import java.util.Base64

abstract class EntryPoint(modules: Seq[Module]) extends IOApp.Simple {

  override def run: IO[Unit] = run0()

  def run0(): IO[Unit] = {

    val logger = consoleLogger[IO]()

    val terminal = PowerShell

    val inputFile  = Paths.get("D:\\events")
    val outputFile = Paths.get("D:\\shell.txt")

    val fileReader = ConsoleEventsReader[IO](inputFile)

    val pipes = modules.map(
      prefetchKeepLatest[IO, ConsoleState]() andThen _.getModulePipe
    )

    fileReader
      .readConsoleEvents()
      .scan(ConsoleState.initial)(ConsoleReducer)
      .changes
      .evalTap(state => logger.debug(f"Console state: ${state.show}"))
      .through(FS2Utils.shareAndCombine(pipes))
      .map(_.flatten)
      .map(toPrompt(_, terminal))
      .map(text => Base64.getEncoder.encodeToString(text.getBytes()))
      .evalMap(writeToFile[IO](outputFile))
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
