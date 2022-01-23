package pl.andrzejressel.wormhole.model

import pl.andrzejressel.wormhole.model.CliMode.GenerateConfig
import pl.andrzejressel.wormhole.terminal.Terminal
import scopt.OParser

import scala.collection.immutable.SortedSet

case class CliConfig(
  mode: CliMode = null,
  terminal: Terminal = null
)
object CliConfig {

  private val builder                  = OParser.builder[CliConfig]
  val parser: OParser[Unit, CliConfig] = {
    import builder._
    OParser.sequence(
      programName("wormhole"),
      head("workhole", "DEV"),
      opt[Terminal]('t', "terminal")
        .required()
        .action((terminal, c) => c.copy(terminal = terminal))
        .text(
          s"Terminal type. Valid values: ${Terminal.namesToValuesMap.keys.to(SortedSet).mkString("[", ",", "]")}"
        ),
      cmd("generate-config")
        .action((_, c) => c.copy(mode = GenerateConfig()))
    )
  }
}

sealed trait CliMode

object CliMode {
  case class GenerateConfig() extends CliMode()
}
