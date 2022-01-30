package pl.andrzejressel.wormhole.test_utils

import org.scalatest._

trait WormholeRetries extends Retries { self: TestSuite =>

  protected val retries = 5

  def retry(test: NoArgTest)(f: () => Outcome): Outcome = {
    if (isRetryable(test)) retryImpl(f, retries)
    else f()
  }

  // https://stackoverflow.com/a/48264490/2511670
  def retryImpl(test: () => Outcome, count: Int): Outcome = {
    val outcome = test()
    outcome match {
      case Failed(_) | Canceled(_) =>
        if (count == 1) test()
        else retryImpl(test, count - 1)
      case other                   => other
    }
  }
}
