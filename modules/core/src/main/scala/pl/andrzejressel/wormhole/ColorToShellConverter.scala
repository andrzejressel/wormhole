package pl.andrzejressel.wormhole

import pl.andrzejressel.wormhole.model.Color
import pl.andrzejressel.wormhole.model.EightBitColor
import pl.andrzejressel.wormhole.model.RGBColor
import pl.andrzejressel.wormhole.model.AnsiColor
import pl.andrzejressel.wormhole.model.BrightColor
import enumeratum._
import pl.andrzejressel.wormhole.ColorToShellConverter.Modifier.BACKGROUND
import pl.andrzejressel.wormhole.ColorToShellConverter.Modifier.FOREGROUND

// https://en.wikipedia.org/wiki/ANSI_escape_code
object ColorToShellConverter {

  val RESET_COLOR = "\u001B[0m"

  private val MAGIC_CODE = '\u001B'

  sealed trait Modifier extends EnumEntry
  object Modifier       extends Enum[Modifier] {
    val values: IndexedSeq[Modifier] = findValues

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
      case c: EightBitColor => getColorCode(modifier, c)
      case c: RGBColor      => getColorCode(modifier, c)
      case c: BrightColor   => getColorCode(modifier, c)
      case c: AnsiColor     => getColorCode(modifier, c)
    }
  }

  private def getColorCode(
    modifier: Modifier,
    color: EightBitColor
  ): String = {
    val modifierNumber = modifier match {
      case FOREGROUND => 38
      case BACKGROUND => 48
    }
    f"$MAGIC_CODE[$modifierNumber;5;${color.color}m"
  }

  private def getColorCode(modifier: Modifier, color: RGBColor): String = {
    val modifierNumber = modifier match {
      case FOREGROUND => 38
      case BACKGROUND => 48
    }
    f"$MAGIC_CODE[$modifierNumber;2;${color.r};${color.g};${color.b}m"
  }

  private def getColorCode(modifier: Modifier, color: AnsiColor): String = {
    val colorNumber = getAnsiColorNumber(color, modifier)
    f"$MAGIC_CODE[${colorNumber}m"
  }

  private def getColorCode(modifier: Modifier, color: BrightColor): String = {
    val colorNumber = getAnsiColorNumber(color.ansiColor, modifier)
    f"$MAGIC_CODE[1;${colorNumber}m"
  }

  private def getAnsiColorNumber(
    ansiColor: AnsiColor,
    modifier: Modifier
  ): Int = {
    val modifierNumber: Int = modifier match {
      case Modifier.BACKGROUND => 10
      case _                   => 0
    }
    colorToCodeMap(ansiColor) + modifierNumber
  }

}
