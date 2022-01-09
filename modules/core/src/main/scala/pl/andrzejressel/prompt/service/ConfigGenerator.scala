package pl.andrzejressel.prompt.service

import pl.andrzejressel.prompt.terminal.Terminal

import java.nio.file.{Files, Path}
import java.util.regex.Matcher
import scala.io.Source

object ConfigGenerator {

  case class Config(
    text: String,
    tempFile: Path,
    tempDir: Path
  )

  def generate(terminal: Terminal): Config = {

    val config = Source
      .fromInputStream(getClass.getResourceAsStream(terminal.getConfigFile()))
      .mkString

    val tempFile     = Files.createTempFile(null, null)
    val tempFileName = tempFile.toFile.getName
    val parent       = tempFile.toFile.getParentFile.getAbsolutePath
    val tempDir      = Files.createTempDirectory(null)

    val text = config
      .replaceAll(
        "TEMP_DIR",
        Matcher.quoteReplacement(tempDir.toFile.getAbsolutePath)
      )
      .replaceAll("TEMP_FILE_NAME", Matcher.quoteReplacement(tempFileName))
      .replaceAll("TEMP_FILE_PARENT", Matcher.quoteReplacement(parent))
      .replaceAll(
        "TEMP_FILE",
        Matcher.quoteReplacement(tempFile.toFile.getAbsolutePath)
      )

    Config(text, tempFile, tempDir)

  }

}
