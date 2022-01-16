package pl.andrzejressel.prompt.model

import cats._
import cats.derived.semiauto
import org.typelevel.ci.CIString
import pl.andrzejressel.prompt.interop.Java.eqPath

import java.nio.file.Path

final case class ConsoleState(
  currentDirectory: Option[Path],
  env: Map[CIString, String]
)
object ConsoleState {
  val initial: ConsoleState = ConsoleState(None, Map())

  implicit val showConsoleState: Show[ConsoleState] = semiauto.show
  implicit val eqConsoleState: Eq[ConsoleState]     = semiauto.eq

  implicit val showPath: Show[Path] = Show.fromToString
}
