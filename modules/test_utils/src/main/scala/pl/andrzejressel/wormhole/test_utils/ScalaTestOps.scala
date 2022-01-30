package pl.andrzejressel.wormhole.test_utils

import org.apache.commons.lang3.SystemUtils
import org.scalatest.{Assertion, Assertions}

object ScalaTestOps {

  def assumeWindows(): Assertion =
    Assertions.assume(SystemUtils.IS_OS_WINDOWS)

  def assumeLinux(): Assertion =
    Assertions.assume(SystemUtils.IS_OS_LINUX)

}
