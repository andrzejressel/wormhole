package pl.andrzejressel.wormhole.model

import enumeratum._
import eu.timepit.refined.numeric._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.cats._
import Color.EightBitNumber
import cats.Eq
import cats.derived.semiauto
sealed trait Color

object Color {
  implicit val colorEq: Eq[Color] = semiauto.eq
  type EightBitNumber = Int Refined Interval.Closed[0, 255]
}

case class EightBitColor(color: EightBitNumber) extends Color

object EightBitColor {
  implicit val eightBitColorEq: Eq[EightBitColor] = semiauto.eq
}

case class RGBColor(
  r: EightBitNumber,
  g: EightBitNumber,
  b: EightBitNumber
) extends Color

object RGBColor {
  implicit val rgbColorEq: Eq[RGBColor] = semiauto.eq
}

sealed trait AnsiColor extends Color with EnumEntry {
  def bright(): BrightColor = BrightColor(this)
}

object AnsiColor extends Enum[AnsiColor] {

  val values: IndexedSeq[AnsiColor] = findValues

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
