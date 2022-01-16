package pl.andrzejressel.prompt.service

import pl.andrzejressel.prompt.terminal.Terminal

import java.nio.file.{Files, Path}
import java.util.regex.Matcher
import scala.io.Source

object ConfigGenerator {

  case class Config(
    text: String,
    consolePromptDirectory: Path,
    consoleEventsDirectory: Path
  )

  def generate(terminal: Terminal): Config = {

    val config = Source
      .fromInputStream(getClass.getResourceAsStream(terminal.getConfigFile()))
      .mkString

    val consoleEventsDirectory = Files.createTempDirectory(null).toAbsolutePath
    val consolePromptDirectory = Files.createTempDirectory(null).toAbsolutePath

    val text = config
      .replaceAll(
        "CONSOLE_EVENTS_DIRECTORY",
        Matcher.quoteReplacement(consoleEventsDirectory.toString)
      )
      .replaceAll(
        "CONSOLE_PROMPT_DIRECTORY",
        Matcher.quoteReplacement(consolePromptDirectory.toString)
      )

    Config(text, consolePromptDirectory, consoleEventsDirectory)

  }

}
