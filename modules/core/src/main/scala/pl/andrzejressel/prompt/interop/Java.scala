package pl.andrzejressel.prompt.interop

import cats.Eq

import java.nio.file.Path

object Java {
  implicit val eqPath: Eq[Path] = Eq.fromUniversalEquals
}
