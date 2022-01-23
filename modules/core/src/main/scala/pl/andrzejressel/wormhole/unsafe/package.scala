package pl.andrzejressel.wormhole

import cats.data.OptionT
import cats.effect._
import cats.implicits._
import pl.andrzejressel.wormhole.model.LoggerComponent.logger

// Package for unsafe methods - ones that can throw exceptions
package object unsafe {

  private[unsafe] def logThrowable[F[_], V](
    message: String
  )(either: Either[Throwable, V])(implicit S: Sync[F]): OptionT[F, V] =
    OptionT(
      either.fold(
        fa = t => logger.warn(message, t) >> S.delay(Option.empty[V]),
        fb = v => S.delay(Option(v))
      )
    )

}
