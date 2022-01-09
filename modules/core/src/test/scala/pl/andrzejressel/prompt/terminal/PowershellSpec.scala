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

    println(config.tempDir)

    val cmd = Array("powershell")
    process = new PtyProcessBuilder()
      .setCommand(cmd)
      .setDirectory(startDirectory.toString)
      .start

    stdout = process.startStdoutGobbler()
    stderr = process.startStderrGobbler()

    process.writeToStdinAndFlush(
      config.text,
      hitEnter = true
    )

  }

  after {
    process.writeToStdinAndFlush("exit", hitEnter = true)
    process.assertProcessTerminatedNormally()
  }

  it should "generate initial info" in {

    eventually { config.tempDir.toFile.listFiles() should have size 2 }

    val consoleEvents =
      config.tempDir.toFile
        .listFiles()
        .map(new FileInputStream(_).readAllBytes())
        .map(new String(_))
        .map[ConsoleEvent](decode[ConsoleEvent](_).value)
        .toList

    val initialDirEvent = consoleEvents
      .collectFirst { case x: ChangeDir => x }

    val initialEnvEvent = consoleEvents
      .collectFirst { case x: SetEnvironment => x }

    initialDirEvent.value.dir shouldBe this.startDirectory.toString
    initialEnvEvent.value.env should contain(ci"os" -> "Windows_NT")

  }

}
