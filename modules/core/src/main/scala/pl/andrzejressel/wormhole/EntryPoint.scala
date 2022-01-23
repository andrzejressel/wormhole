package pl.andrzejressel.wormhole

import cats.effect._
import cats.implicits._
import fs2.io.file.Files.forAsync
import io.odin.consoleLogger
import pl.andrzejressel.wormhole.ColorToShellConverter.{
  RESET_COLOR,
  getBackgroundEscapeCode,
  getForegroundEscapeCode
}
import pl.andrzejressel.wormhole.model._
import pl.andrzejressel.wormhole.terminal.Terminal
import pl.andrzejressel.wormhole.unsafe.File.writeToFile
import pl.andrzejressel.wormhole.utils.FS2Utils
import pl.andrzejressel.wormhole.utils.FS2Utils.prefetchKeepLatest
import scopt.OParser

import java.nio.file.Paths
import java.util.Base64

abstract class EntryPoint(config: Config) extends IOApp {

  private val logger = consoleLogger[IO]()

  override def run(args: List[String]): IO[ExitCode] = {

    OParser.parse(CliConfig.parser, args, CliConfig()) match {
      case Some(value) => run0(value).as(ExitCode.Success)
      case None        => IO(ExitCode.Error)
    }

  }

  def run0(cliConfig: CliConfig): IO[Unit] = {

    val terminal = cliConfig.terminal

    val inputFile  = Paths.get("D:\\events")
    val outputFile = Paths.get("D:\\shell.txt")

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
