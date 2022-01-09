package pl.andrzejressel.prompt.utils

import org.apache.commons.lang3.SystemUtils
import org.scalactic.source
import org.scalatest.{BeforeAndAfter, Outcome, TestSuite}
import pl.andrzejressel.prompt.utils.ScalaTestOps.assumeWindows

trait WindowsOnly extends TestSuite with BeforeAndAfter {
  override protected def withFixture(test: NoArgTest): Outcome = {
    assumeWindows()
    super.withFixture(test)
  }

  override protected def before(
    fun: => Any
  )(implicit pos: source.Position): Unit = {
    if (SystemUtils.IS_OS_WINDOWS) {
      super.before(fun)(pos)
    }
  }

  override protected def after(
    fun: => Any
  )(implicit pos: source.Position): Unit = {
    if (SystemUtils.IS_OS_WINDOWS) {
      super.after(fun)(pos)
    }
  }

}
