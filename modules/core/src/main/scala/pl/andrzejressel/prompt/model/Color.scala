package pl.andrzejressel.prompt.model

import enumeratum._
import eu.timepit.refined.numeric._
import eu.timepit.refined.api.Refined
import Color.EightBitNumber

sealed trait Color

object Color {
  type EightBitNumber = Int Refined Interval.Closed[0, 255]
}

case class EightBitColor(color: EightBitNumber) extends Color

case class RGBColor(
  r: EightBitNumber,
  g: EightBitNumber,
  b: EightBitNumber
) extends Color

sealed trait AnsiColor extends Color with EnumEntry {
  def bright(): BrightColor = BrightColor(this)
}

object AnsiColor extends Enum[AnsiColor] {

  val values = findValues

  case object Black   extends AnsiColor
  case object Red     extends AnsiColor
  case object Green   extends AnsiColor
  case object Yellow  extends AnsiColor
  case object Blue    extends AnsiColor
  case object Magenta extends AnsiColor
  case object Cyan    extends AnsiColor
  case object White   extends AnsiColor
}

case class BrightColor(ansiColor: AnsiColor) extends Color
