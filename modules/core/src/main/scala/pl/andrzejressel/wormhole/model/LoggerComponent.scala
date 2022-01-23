package pl.andrzejressel.wormhole.model

import cats.effect.kernel.Sync
import io.odin.{Logger, consoleLogger}

object LoggerComponent {
  def logger[F[_]: Sync]: Logger[F] = consoleLogger()
}
