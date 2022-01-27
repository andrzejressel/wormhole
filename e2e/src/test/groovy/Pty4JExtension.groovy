import com.pty4j.PtyProcess
import com.pty4j.PtyTest

class Pty4JExtension {

    static void assertProcessTerminatedNormally(final Process self) {
        PtyTest.assertProcessTerminatedNormally(self)
    }

    static void writeToStdinAndFlush(final PtyProcess self, final String input, final Boolean hitEnter) {
        PtyTest.writeToStdinAndFlush(self, input, hitEnter)
    }

    static void slowType(final PtyProcess self, String text) {
        for (c in text) {
            self.writeToStdinAndFlush(c, false)
            Thread.sleep(100)
        }
        self.writeToStdinAndFlush("", true)
    }

    static PtyTest.Gobbler startStdoutGobbler(final PtyProcess self) { PtyTest.startStdoutGobbler(self) }

    static PtyTest.Gobbler startStderrGobbler(final PtyProcess self) { PtyTest.startStderrGobbler(self) }

}
