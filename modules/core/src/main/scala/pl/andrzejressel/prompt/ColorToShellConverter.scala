package pl.andrzejressel.prompt

import pl.andrzejressel.prompt.model.Color
import pl.andrzejressel.prompt.model.EightBitColor
import pl.andrzejressel.prompt.model.RGBColor
import pl.andrzejressel.prompt.model.AnsiColor
import pl.andrzejressel.prompt.model.BrightColor
import enumeratum._
import pl.andrzejressel.prompt.ColorToShellConverter.Modifier.BACKGROUND
import pl.andrzejressel.prompt.ColorToShellConverter.Modifier.FOREGROUND

// https://en.wikipedia.org/wiki/ANSI_escape_code
object ColorToShellConverter {

  val RESET_COLOR = "\u001B[0m"

  private val MAGIC_CODE = '\u001B'

  sealed trait Modifier extends EnumEntry
  object Modifier       extends Enum[Modifier] {
    val values = findValues

    case object BACKGROUND extends Modifier
    case object FOREGROUND extends Modifier
  }

  private def colorToCodeMap(ansiColor: AnsiColor): Int = ansiColor match {
    case AnsiColor.Black   => 30
    case AnsiColor.Red     => 31
    case AnsiColor.Green   => 32
    case AnsiColor.Yellow  => 33
    case AnsiColor.Blue    => 34
    case AnsiColor.Magenta => 35
    case AnsiColor.Cyan    => 36
    case AnsiColor.White   => 37
  }

  def getForegroundEscapeCode(color: Color): String = {
    getColorCode(Modifier.FOREGROUND, color)
  }

  def getBackgroundEscapeCode(color: Color): String = {
    getColorCode(Modifier.BACKGROUND, color)
  }

  def getColorCode(modifier: Modifier, color: Color): String = {
    color match {
      case c: EightBitColor => _getColorCode(modifier, c)
      case c: RGBColor      => _getColorCode(modifier, c)
      case c: BrightColor   => _getColorCode(modifier, c)
      case c: AnsiColor     => _getColorCode(modifier, c)
    }
  }

  private def _getColorCode(
    modifier: Modifier,
    color: EightBitColor
  ): String = {
    val modifierNumber = modifier match {
      case BACKGROUND => 48
      case FOREGROUND => 38
    }
    f"$MAGIC_CODE[$modifierNumber;5;${color.color}m"
  }

  private def _getColorCode(modifier: Modifier, color: RGBColor): String = {
    val modifierNumber = modifier match {
      case FOREGROUND => 38
      case BACKGROUND => 48
    }
    f"$MAGIC_CODE[$modifierNumber;2;${color.r};${color.g};${color.b}m"
  }

  private def _getColorCode(modifier: Modifier, color: AnsiColor): String = {
    var colorNumber = colorToCodeMap(color)
    if (modifier == Modifier.BACKGROUND) {
      colorNumber += 10
    }
    f"$MAGIC_CODE[${colorNumber}m"
  }

  private def _getColorCode(modifier: Modifier, color: BrightColor): String = {
    var colorNumber = colorToCodeMap(color.ansiColor)
    if (modifier == Modifier.BACKGROUND) {
      colorNumber += 10
    }
    f"$MAGIC_CODE[1;${colorNumber}m"
  }

}
