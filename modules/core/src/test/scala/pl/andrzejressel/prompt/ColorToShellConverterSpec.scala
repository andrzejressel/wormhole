package pl.andrzejressel.prompt

import eu.timepit.refined.scalacheck.numeric._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.flatspec._
import org.scalatest.matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pl.andrzejressel.prompt.model.Color.EightBitNumber
import pl.andrzejressel.prompt.model.{
  AnsiColor,
  BrightColor,
  EightBitColor,
  RGBColor
}

class ColorToShellConverterSpec
    extends AnyFlatSpec
    with should.Matchers
    with ScalaCheckDrivenPropertyChecks {

  implicit val dataArb: Arbitrary[EightBitColor] = Arbitrary(
    Gen.resultOf(EightBitColor)
  )

  it should "generate foreground color for 8 bit color" in {
    forAll("color")((color: EightBitColor) => {
      val escapeCode = ColorToShellConverter.getForegroundEscapeCode(color)
      escapeCode shouldBe f"\u001B[38;5;${color.color}m"
    })
  }

  it should "generate background color for 8 bit color" in {
    forAll("color")((color: EightBitColor) => {
      val escapeCode = ColorToShellConverter.getBackgroundEscapeCode(color)
      escapeCode shouldBe f"\u001B[48;5;${color.color}m"
    })
  }

  it should "generate foreground color for 24 bit (RGB) color" in {
    forAll("R", "G", "B")(
      (r: EightBitNumber, g: EightBitNumber, b: EightBitNumber) => {
        val color      = RGBColor(r, g, b)
        val escapeCode = ColorToShellConverter.getForegroundEscapeCode(color)
        escapeCode shouldBe f"\u001B[38;2;$r;$g;${b}m"
      }
    )
  }

  it should "generate background color for 24 bit (RGB) color" in {
    forAll("R", "G", "B")(
      (r: EightBitNumber, g: EightBitNumber, b: EightBitNumber) => {
        val color      = RGBColor(r, g, b)
        val escapeCode = ColorToShellConverter.getBackgroundEscapeCode(color)
        escapeCode shouldBe f"\u001B[48;2;$r;$g;${b}m"
      }
    )
  }

  it should "generate foreground color for 3/4 bit colors" in {
    ColorToShellConverter.getForegroundEscapeCode(
      AnsiColor.Red
    ) shouldBe "\u001B[31m"

    ColorToShellConverter.getForegroundEscapeCode(
      BrightColor(AnsiColor.Red)
    ) shouldBe "\u001B[1;31m"
  }

  it should "generate background color for 3/4 bit colors" in {
    ColorToShellConverter.getBackgroundEscapeCode(
      AnsiColor.Red
    ) shouldBe "\u001B[41m"

    ColorToShellConverter.getBackgroundEscapeCode(
      BrightColor(AnsiColor.Red)
    ) shouldBe "\u001B[1;41m"
  }
}
