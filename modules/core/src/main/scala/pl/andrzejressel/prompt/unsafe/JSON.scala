package pl.andrzejressel.prompt.unsafe

import cats.data.OptionT
import cats.effect.kernel.Sync
import io.circe.Decoder

object JSON {
  def decode[F[_], A](json: String)(implicit
    decoder: Decoder[A],
    S: Sync[F]
  ): OptionT[F, A] =
    logThrowable(s"Cannot decode JSON: ${json}")(io.circe.parser.decode(json))
}
