package pl.andrzejressel.wormhole.utils

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}

trait PromptEventually extends Eventually {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(15, Seconds)),
    interval = scaled(Span(1, Seconds))
  )
}
