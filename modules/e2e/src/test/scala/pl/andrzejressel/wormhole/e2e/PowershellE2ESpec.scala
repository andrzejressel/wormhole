package pl.andrzejressel.wormhole.e2e

import com.pty4j.PtyProcessBuilder
import org.scalatest.{Outcome, Retries}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.tagobjects.Retryable
import pl.andrzejressel.wormhole.test_utils.PtyTestOps.{
  ProcessOps,
  PtyProcessOps
}
import tags.WindowsOnly
import java.lang.Thread.sleep

@WindowsOnly
class PowershellE2ESpec extends AnyFlatSpec with should.Matchers with Retries {

  override def withFixture(test: NoArgTest): Outcome = {
    if (isRetryable(test))
      withRetry { super.withFixture(test) }
    else
      super.withFixture(test)
  }

  it should "run executable" taggedAs Retryable in {

    val cmd     = Array("powershell", "-NoProfile")
    val process = new PtyProcessBuilder()
      .setCommand(cmd)
      .setInitialColumns(80)
      .setInitialRows(20)
      .start()

    val stdout = process.startStdoutGobbler()
    val stderr = process.startStderrGobbler()

    val setEnv = f"$$env:WORMHOLE_COMMAND=\"${Utils.executable}\""
    val invoke =
      f"Invoke-Expression ((${Utils.executable} generate-config -t powershell) | Out-String)"

    process.writeToStdinAndFlush(
      s"powersession.exe rec \"${Utils.outputDir.resolve("PowershellE2ESpec.txt")}\"",
      hitEnter = true
    )
    sleep(4000)
    process.writeToStdinAndFlush(setEnv, hitEnter = true)
    sleep(1000)
    process.writeToStdinAndFlush(invoke, hitEnter = true)
    sleep(4000)
    process.slowType("whoami")
    sleep(4000)
    process.slowType("exit")
    sleep(1000)
    process.writeToStdinAndFlush("exit", hitEnter = true)

    try {
      process.assertProcessTerminatedNormally()
    } finally {
      process.destroyForcibly()

      println("OUTPUT:")
      println(stdout.getCleanOutput)
      println()
      println("ERROR:")
      println(stderr.getCleanOutput)
    }

  }

}
