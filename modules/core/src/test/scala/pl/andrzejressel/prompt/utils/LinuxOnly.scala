package pl.andrzejressel.prompt.utils

import org.scalatest.{Outcome, TestSuite}
import pl.andrzejressel.prompt.utils.ScalaTestOps.assumeLinux

trait LinuxOnly extends TestSuite {
  override protected def withFixture(test: NoArgTest): Outcome = {
    assumeLinux()
    super.withFixture(test)
  }
}
