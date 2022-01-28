package pl.andrzejressel.wormhole.usecase

import cats.effect.IO
import cats.implicits._
import pl.andrzejressel.wormhole.ColorToShellConverter.{
  RESET_COLOR,
  getBackgroundEscapeCode,
  getForegroundEscapeCode
}
import pl.andrzejressel.wormhole.ConsoleEventsReader
import pl.andrzejressel.wormhole.model._
import pl.andrzejressel.wormhole.terminal.Terminal
import pl.andrzejressel.wormhole.unsafe.File.writeToFile
import pl.andrzejressel.wormhole.utils.FS2Utils
import pl.andrzejressel.wormhole.utils.FS2Utils.prefetchKeepLatest

import java.util.Base64

case class StartApplicationUseCase() {

  private val logger = LoggerComponent.logger[IO]

  def run(terminal: Terminal, mode: CliMode.Start, config: Config): IO[Unit] = {

    val inputFile  = mode.consoleSourcePath
    val outputFile = mode.consoleSinkPath.resolve("prompt.txt")

    val fileReader = ConsoleEventsReader[IO](inputFile)

    val pipes = config.modules.map(
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

  private def toPrompt(segments: Seq[Segment], terminal: Terminal): String = {

    segments.map { segment =>
      val bg = getBackgroundEscapeCode(segment.backgroundColor)
      val fg = getForegroundEscapeCode(segment.textColor)

      terminal.escapeColor(bg + fg) + segment.text + terminal.escapeColor(
        RESET_COLOR
      )
    }.mkString + "\n\ue0b0 "

  }

}
