package pl.andrzejressel.prompt.utils

import com.sun.jna.platform.win32.WinDef.DWORD
import org.apache.commons.lang3.SystemUtils
import org.scalactic.source
import org.scalatest.{BeforeAndAfter, Outcome, TestSuite}
import pl.andrzejressel.prompt.utils.ScalaTestOps.assumeWindows

import java.nio.file.Path

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

  implicit class WindowsPath(val path: Path) {
    def expand(): Path = {

      val fileApi = FileApi.INSTANCE

      val bufferSize = 32_767
      val buffer     = new Array[Char](bufferSize)
      fileApi.GetLongPathName(path.toString, buffer, new DWORD(bufferSize))

      val p = new String(buffer.takeWhile(_ != 0))

      Path.of(p)
    }
  }

}
