package pl.andrzejressel.wormhole.interop

import io.circe.{Decoder, KeyDecoder}
import org.typelevel.ci.CIString

object CaseInsensitive {
  implicit val CIStringDecoder: Decoder[CIString] =
    Decoder[String].map(CIString(_))

  implicit val decodeCIString: KeyDecoder[CIString] =
    KeyDecoder[String].map(CIString(_))

}
