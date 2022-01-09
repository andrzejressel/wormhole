package pl.andrzejressel.prompt.utils

import org.scalatest.{Outcome, TestSuite}
import pl.andrzejressel.prompt.utils.ScalaTestOps.assumeWindows

trait WindowsOnly extends TestSuite {
  override protected def withFixture(test: NoArgTest): Outcome = {
    assumeWindows()
    super.withFixture(test)
  }
}
