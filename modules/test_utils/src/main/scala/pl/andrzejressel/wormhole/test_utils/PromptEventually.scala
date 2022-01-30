package pl.andrzejressel.wormhole.test_utils

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}

trait PromptEventually extends Eventually {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(
    timeout = Span(15, Seconds),
    interval = Span(1, Seconds)
  )
}
