package pl.andrzejressel.wormhole.model

import cats.Eq
import cats.derived.semiauto

case class Segment(
  text: String,
  textColor: Color,
  backgroundColor: Color
)

object Segment {
  implicit val segmentEq: Eq[Segment] = semiauto.eq
}
