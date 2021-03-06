package pl.andrzejressel.wormhole.e2e

import com.pty4j.PtyProcessBuilder
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import pl.andrzejressel.wormhole.test_utils.PtyTestOps.{
  ProcessOps,
  PtyProcessOps
}
import pl.andrzejressel.wormhole.test_utils.WindowsOnly

import java.lang.Thread.sleep

class PowershellE2ESpec
    extends AnyFlatSpec
    with should.Matchers
    with WindowsOnly {

  it should "run executable" in {

    val cmd     = Array("powershell", "-NoProfile")
    val process = new PtyProcessBuilder()
      .setCommand(cmd)
      .setInitialColumns(80)
      .setInitialRows(20)
      .start()

    process.startStdoutGobbler()
    process.startStderrGobbler()

    val setEnv = f"$$env:WORMHOLE_COMMAND=\"${Utils.executable}\""
    val invoke =
      f"Invoke-Expression ((${Utils.executable} generate-config -t powershell) | Out-String)"

    process.writeToStdinAndFlush(
      s"powersession.exe rec \"${Utils.outputDir.resolve("PowershellE2ESpec.txt")}\"",
      hitEnter = true
    )
    sleep(1000)
    process.slowType(setEnv)
    sleep(1000)
    process.slowType(invoke)
    sleep(4000)
    process.slowType("whoami")
    sleep(4000)
    process.slowType("exit")
    sleep(1000)
    process.writeToStdinAndFlush("exit", hitEnter = true)

    process.assertProcessTerminatedNormally()

  }

}
