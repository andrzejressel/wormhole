package pl.andrzejressel.prompt.terminal

import com.pty4j.PtyTest.Gobbler
import com.pty4j.{PtyProcess, PtyProcessBuilder}
import io.circe.parser.decode
import org.scalatest.BeforeAndAfter
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import org.scalatest.flatspec._
import org.scalatest.matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.typelevel.ci.CIStringSyntax
import pl.andrzejressel.prompt.model.{ChangeDir, ConsoleEvent, SetEnvironment}
import pl.andrzejressel.prompt.service.ConfigGenerator
import pl.andrzejressel.prompt.service.ConfigGenerator.Config
import pl.andrzejressel.prompt.terminal.Terminal.PowerShell
import pl.andrzejressel.prompt.utils.PtyTestOps._
import pl.andrzejressel.prompt.utils.{PromptEventually, WindowsOnly}

import java.io.FileInputStream
import java.nio.file.{Files, Path}
import java.util.Base64

class PowershellSpec
    extends AnyFlatSpec
    with should.Matchers
    with ScalaCheckDrivenPropertyChecks
    with BeforeAndAfter
    with WindowsOnly
    with PromptEventually {

  var config: Config       = _
  var startDirectory: Path = _
  var process: PtyProcess  = _
  var stdout: Gobbler      = _
  var stderr: Gobbler      = _

  before {
    config = ConfigGenerator.generate(PowerShell)
    startDirectory = Files.createTempDirectory(null)

    val script = Files.createTempFile(null, ".ps1")
    Files.writeString(script, config.text)
    Files.writeString(config.consolePromptDirectory.resolve("prompt.txt"), "")

    val cmd = Array("powershell")
    process = new PtyProcessBuilder()
      .setCommand(cmd)
      .setDirectory(startDirectory.toString)
      .start

    stdout = process.startStdoutGobbler()
    stderr = process.startStderrGobbler()

    process.writeToStdinAndFlush(
      script.toAbsolutePath.toString,
      hitEnter = true
    )

  }

  after {
    process.writeToStdinAndFlush("exit", hitEnter = true)
    process.assertProcessTerminatedNormally()
  }

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

  it should "apply prompt" in {

    // Wait for Powershell to initialize FileSystemWatcher
    Thread.sleep(1000)

    Files.writeString(
      config.consolePromptDirectory.resolve("prompt.txt"),
      Base64.getEncoder.encodeToString("My prompt>".getBytes)
    )

    stdout.assertEndsWith("My prompt>")

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
