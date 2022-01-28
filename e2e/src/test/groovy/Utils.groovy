import java.nio.file.Path

class Utils {

    public static Path executable = Path.of("",  "..", "modules", "example", "target", "native-image", "example.exe").toAbsolutePath()

}
