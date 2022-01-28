package pl.andrzejressel.wormhole.interop

import enumeratum._
import scopt.Read
import scopt.Read.reads

import java.nio.file.Path

object Scopt {

  implicit val scoptPathRead: Read[Path] =
    reads { Path.of(_) }

  implicit class EnumOpt[A <: EnumEntry](e: Enum[A]) {
    val scoptRead: Read[A] = reads { e.withNameInsensitive }
  }

}
