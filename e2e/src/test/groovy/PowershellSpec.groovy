import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import spock.lang.Specification

import java.nio.file.Files

class PowershellSpec extends Specification {

    def "maximum of two numbers"() {
        when:
        Files.exists(Utils.executable)
        def cmd = ["powershell", "-NoProfile"] as String[]
        def process = new PtyProcessBuilder()
                .setCommand(cmd)
                .setInitialColumns(80)
                .setInitialRows(20)
                .start()

        def stdout = process.startStdoutGobbler()
        def stderr = process.startStderrGobbler()

        def setEnv = "\$env:WORMHOLE_COMMAND=\"${Utils.executable}\""
        def invoke = "Invoke-Expression ((${Utils.executable} generate-config -t powershell) | Out-String)"

        process.writeToStdinAndFlush("powersession.exe rec D:\\b.txt", true)
        sleep(1000)
        process.slowType(setEnv)
        sleep(1000)
        process.slowType(invoke)
        sleep(4000)
        process.slowType("whoami")
        sleep(4000)
        process.slowType("exit")
        sleep(1000)
        process.writeToStdinAndFlush("exit", true)

        process.assertProcessTerminatedNormally()

        then:
        true

    }

}
