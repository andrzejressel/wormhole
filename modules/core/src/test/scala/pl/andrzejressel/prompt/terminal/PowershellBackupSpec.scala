package pl.andrzejressel.prompt.terminal

import com.pty4j.PtyProcessBuilder
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec._
import org.scalatest.matchers._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pl.andrzejressel.prompt.service.ConfigGenerator
import pl.andrzejressel.prompt.terminal.Terminal.PowerShell
import pl.andrzejressel.prompt.utils.PtyTestOps._
import pl.andrzejressel.prompt.utils.{PromptEventually, WindowsOnly}

import java.nio.file.Files
import scala.jdk.CollectionConverters._

class PowershellBackupSpec
    extends AnyFlatSpec
    with should.Matchers
    with ScalaCheckDrivenPropertyChecks
    with BeforeAndAfter
    with WindowsOnly
    with PromptEventually {

  it should "work" in {

    val cmd     = Array("powershell")
    val env     = System.getenv.asScala.toMap
    val process =
      new PtyProcessBuilder().setCommand(cmd).setEnvironment(env.asJava).start

    val stdout = process.startStdoutGobbler()
    process.startStderrGobbler()

    process.writeToStdinAndFlush(
      """function prompt {"Hello, World > "}""",
      hitEnter = true
    )
    stdout.assertEndsWith("Hello, World >")

    process.writeToStdinAndFlush("quit", hitEnter = true)
    process.writeToStdinAndFlush("exit", hitEnter = true)
    process.assertProcessTerminatedNormally()

  }

  it should "work2" in {

    val cmd     = Array("powershell")
    val env     = System.getenv.asScala.toMap
    val process =
      new PtyProcessBuilder().setCommand(cmd).setEnvironment(env.asJava).start

    val stdout = process.startStdoutGobbler()
    process.startStderrGobbler()

    process.writeToStdinAndFlush(
      "d:\\MojeProgramy\\prompt\\shell\\powershell\\prompt_copy.ps1",
      hitEnter = true
    )
    stdout.assertEndsWith("Work done...>")
    Thread.sleep(1000)
    process.writeToStdinAndFlush("echo 'test'", hitEnter = true)
    stdout.assertEndsWith("Work done...>")
    Thread.sleep(1000)
    process.writeToStdinAndFlush("exit", hitEnter = true)
    process.assertProcessTerminatedNormally()

  }

  it should "work3" in {

    val config = ConfigGenerator.generate(PowerShell)

    val startDirectory = Files.createTempDirectory(null)

    val cmd     = Array("powershell")
    val process =
      new PtyProcessBuilder()
        .setCommand(cmd)
        .setDirectory(startDirectory.toString)
        .start

    process.startStdoutGobbler()
    process.startStderrGobbler()

    process.writeToStdinAndFlush(
      config.text,
      hitEnter = true
    )

    println(config.tempDir)

    eventually { config.tempDir.toFile.listFiles() should have size 2 }

//    "Windows_NT"

//    println(config.tempDir)

//    process.writeToStdinAndFlush(
//      config.text,
//      hitEnter = true
//    )
////    stdout.assertEndsWith("Work done...>")
//    process.writeToStdinAndFlush("cd D:\\", hitEnter = true)
//    Thread.sleep(1000)
//    process.writeToStdinAndFlush("echo 'test'", hitEnter = true)
//    stdout.assertEndsWith("Work done...>")
//    Thread.sleep(1000)
    process.writeToStdinAndFlush("exit", hitEnter = true)
    process.assertProcessTerminatedNormally()

  }

}
