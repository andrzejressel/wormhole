package pl.andrzejressel.wormhole.model

import pl.andrzejressel.wormhole.interop.Scopt._
import pl.andrzejressel.wormhole.model.CliMode.{GenerateConfig, Start}
import pl.andrzejressel.wormhole.terminal.Terminal
import scopt.OParser

import java.nio.file.{Files, Path}
import scala.collection.immutable.SortedSet

case class CliConfig(
  mode: CliMode = null,
  terminal: Terminal = null
)
object CliConfig {

  private val builder                  = OParser.builder[CliConfig]
  val parser: OParser[Unit, CliConfig] = {
    import builder._

    def verifyIsFile(path: Path): Either[String, Unit] =
      if (!Files.exists(path)) {
        failure(s"Directory $path does not exist")
      } else if (!Files.isDirectory(path)) {
        failure(s"$path is not directory")
      } else {
        success
      }

    OParser.sequence(
      programName("wormhole"),
      head("workhole", "DEV"),
      opt[Terminal]('t', "terminal")
        .required()
        .action((terminal, c) => c.copy(terminal = terminal))
        .text(
          s"Terminal type. Valid values: ${Terminal.namesToValuesMap.keys.to(SortedSet).mkString("[", ",", "]")}"
        ),
      cmd("start")
        .action((_, c) => c.copy(mode = Start(null, null)))
        .children(
          opt[Path]("console_events_path")
            .validate(verifyIsFile)
            .required()
            .action((p, c) =>
              c.copy(mode =
                c.mode.asInstanceOf[Start].copy(consoleSourcePath = p)
              )
            ),
          opt[Path]("console_prompt_path")
            .validate(verifyIsFile)
            .required()
            .action((p, c) =>
              c.copy(mode =
                c.mode.asInstanceOf[Start].copy(consoleSinkPath = p)
              )
            )
        ),
      cmd("generate-config")
        .action((_, c) => c.copy(mode = GenerateConfig()))
    )
  }
}

sealed trait CliMode

object CliMode {
  case class Start(
    consoleSourcePath: Path,
    consoleSinkPath: Path
  ) extends CliMode()
  case class GenerateConfig() extends CliMode()
}
