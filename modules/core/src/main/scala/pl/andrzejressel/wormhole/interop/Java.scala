package pl.andrzejressel.wormhole.interop

import cats.Eq
import io.circe.Decoder

import java.nio.file.Path

object Java {
  implicit val eqPath: Eq[Path] = Eq.fromUniversalEquals

  implicit val pathDecoder: Decoder[Path] = Decoder[String].map(Path.of(_))
}
