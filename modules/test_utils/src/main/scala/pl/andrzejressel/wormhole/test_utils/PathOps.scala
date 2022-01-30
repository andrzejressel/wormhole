package pl.andrzejressel.wormhole.test_utils

import com.sun.jna.platform.win32.WinDef.DWORD
import pl.andrzejressel.wormhole.utils.FileApi

import java.nio.file.Path

object PathOps {
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
