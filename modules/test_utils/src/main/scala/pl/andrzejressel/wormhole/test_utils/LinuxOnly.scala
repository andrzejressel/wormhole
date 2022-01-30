package pl.andrzejressel.wormhole.test_utils

import org.apache.commons.lang3.SystemUtils
import org.scalactic.source
import org.scalatest.{BeforeAndAfter, Outcome, TestSuite}
import pl.andrzejressel.wormhole.test_utils.ScalaTestOps.assumeLinux

trait LinuxOnly extends TestSuite with BeforeAndAfter {
  override protected def withFixture(test: NoArgTest): Outcome = {
    assumeLinux()
    super.withFixture(test)
  }

  override protected def before(
    fun: => Any
  )(implicit pos: source.Position): Unit = {
    if (SystemUtils.IS_OS_LINUX) {
      super.before(fun)(pos)
    }
  }

  override protected def after(
    fun: => Any
  )(implicit pos: source.Position): Unit = {
    if (SystemUtils.IS_OS_LINUX) {
      super.after(fun)(pos)
    }
  }

}
