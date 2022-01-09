package pl.andrzejressel.prompt.model

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.typelevel.ci.CIString
import pl.andrzejressel.prompt.interop.CaseInsensitive._
import pl.andrzejressel.prompt.interop.Java._

import java.nio.file.Path

sealed trait ConsoleEvent {}

object ConsoleEvent {

  implicit val genConsoleEventConfig: Configuration =
    Configuration.default
      .withDiscriminator("type")
      .withSnakeCaseConstructorNames

  implicit val consoleEventDecoder: Decoder[ConsoleEvent] =
    deriveConfiguredDecoder

}

case class NewConsole(pid: Int)                       extends ConsoleEvent
case class ChangeDir(dir: Path)                       extends ConsoleEvent
case class SetEnvironment(env: Map[CIString, String]) extends ConsoleEvent
