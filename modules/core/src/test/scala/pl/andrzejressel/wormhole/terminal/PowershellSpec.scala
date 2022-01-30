package pl.andrzejressel.wormhole.terminal

import com.pty4j.PtyTest.Gobbler
import com.pty4j.{PtyProcess, PtyProcessBuilder}
import io.circe.parser.decode
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import org.scalatest.Outcome
import org.scalatest.flatspec._
import org.scalatest.matchers._
import org.scalatest.tagobjects.Retryable
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.typelevel.ci.CIStringSyntax
import pl.andrzejressel.wormhole.model.{ChangeDir, ConsoleEvent, SetEnvironment}
import pl.andrzejressel.wormhole.service.ConfigGenerator
import pl.andrzejressel.wormhole.service.ConfigGenerator.Config
import pl.andrzejressel.wormhole.terminal.Terminal.PowerShell
import pl.andrzejressel.wormhole.test_utils.PathOps.WindowsPath
import pl.andrzejressel.wormhole.test_utils.PtyTestOps.{
  ProcessOps,
  PtyProcessOps
}
import pl.andrzejressel.wormhole.test_utils.{
  WormholeEventually,
  WormholeRetries
}

import java.io.FileInputStream
import java.nio.file.{Files, Path}
import java.util.Base64
import scala.jdk.CollectionConverters._
import scala.util.Random

@tags.WindowsOnly
class PowershellSpec
    extends AnyFlatSpec
    with should.Matchers
    with ScalaCheckDrivenPropertyChecks
    with WormholeEventually
    with WormholeRetries {

  var config: Config       = _
  var startDirectory: Path = _
  var process: PtyProcess  = _
  var stdout: Gobbler      = _
  var stderr: Gobbler      = _

  override def withFixture(test: NoArgTest): Outcome = retry(test)(() => {
    config = ConfigGenerator.generate(PowerShell)
    startDirectory = Files.createTempDirectory(null)

    val script = Files.createTempFile(null, ".ps1")
    Files.writeString(script, config.text)
    Files.writeString(config.consolePromptDirectory.resolve("prompt.txt"), "")

    val env    = System.getenv.asScala.to(Map)
    val newEnv = env + ("WORMHOLE_COMMAND" -> "none")

    val cmd = Array("powershell", "-NoProfile")
    process = new PtyProcessBuilder()
      .setCommand(cmd)
      .setEnvironment(newEnv.asJava)
      .setDirectory(startDirectory.toString)
      .start

    stdout = process.startStdoutGobbler()
    stderr = process.startStderrGobbler()

    process.writeToStdinAndFlush(
      script.toAbsolutePath.toString,
      hitEnter = true
    )

    try super.withFixture(test)
    finally {
      process.writeToStdinAndFlush("exit", hitEnter = true)
      process.assertProcessTerminatedNormally()
    }
  })

  it should "generate initial info" in {

    eventually {
      config.consoleEventsDirectory.toFile.listFiles() should have size 2
    }

    val consoleEvents =
      config.consoleEventsDirectory.toFile
        .listFiles()
        .map(new FileInputStream(_).readAllBytes())
        .map(new String(_))
        .map(decode[ConsoleEvent](_).value)
        .toList

    consoleEvents should have size 2

    val initialDirEvent = consoleEvents
      .collectFirst { case x: ChangeDir => x }

    val initialEnvEvent = consoleEvents
      .collectFirst { case x: SetEnvironment => x }

    initialDirEvent.value.dir.expand() shouldBe this.startDirectory.expand()
    initialEnvEvent.value.env should contain(ci"os" -> "Windows_NT")

  }

  it should "apply prompt" taggedAs Retryable in {

    // Wait for Powershell to initialize FileSystemWatcher
    Thread.sleep(Random.between(3000, 7000))

    Files.writeString(
      config.consolePromptDirectory.resolve("prompt.txt"),
      Base64.getEncoder.encodeToString("My prompt\n>".getBytes)
    )

    stdout.assertEndsWith("My prompt\r\n>")

  }

  it should "should send event on directory change" in {

    eventually {
      config.consoleEventsDirectory.toFile.listFiles() should have size 2
    }
    config.consoleEventsDirectory.toFile.listFiles().foreach(_.delete())
    val newDir = Files.createTempDirectory(null)

    process.writeToStdinAndFlush(f"cd \"$newDir\"", hitEnter = true)

    eventually {
      config.consoleEventsDirectory.toFile.listFiles() should have size 1
    }

    val event =
      config.consoleEventsDirectory.toFile
        .listFiles()
        .map(new FileInputStream(_).readAllBytes())
        .map(new String(_))
        .map(decode[ConsoleEvent](_).value)
        .toList
        .head
        .asInstanceOf[ChangeDir]

    event.dir.expand() shouldBe newDir.expand()

  }

}
