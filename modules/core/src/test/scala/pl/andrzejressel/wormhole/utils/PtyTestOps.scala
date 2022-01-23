package pl.andrzejressel.wormhole.utils

import com.pty4j.PtyTest.Gobbler
import com.pty4j.{PtyProcess, PtyTest}

object PtyTestOps {
  implicit class ProcessOps(val process: Process) {
    def assertProcessTerminatedNormally(): Unit =
      PtyTest.assertProcessTerminatedNormally(process)
  }

  implicit class PtyProcessOps(val process: PtyProcess) {
    def writeToStdinAndFlush(input: String, hitEnter: Boolean): Unit =
      PtyTest.writeToStdinAndFlush(process, input, hitEnter)

    def startStdoutGobbler(): Gobbler = PtyTest.startStdoutGobbler(process)
    def startStderrGobbler(): Gobbler = PtyTest.startStderrGobbler(process)
  }
}
