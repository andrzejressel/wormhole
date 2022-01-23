package pl.andrzejressel.wormhole.interop

import cats.data.OptionT

object FS2 {
  implicit def option2Stream[F[_], O](
    option: Option[O]
  ): fs2.Stream[F, O] = {
    fs2.Stream.fromOption(option)
  }

  implicit def optionT2Stream[F[_], O](
    option: OptionT[F, O]
  ): fs2.Stream[F, O] = {
    fs2.Stream.eval(option.value).unNone
  }
}
