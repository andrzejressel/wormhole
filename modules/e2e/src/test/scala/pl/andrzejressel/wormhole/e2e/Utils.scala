package pl.andrzejressel.wormhole.e2e

import java.nio.file.{Files, Path}

object Utils {

  val executable: Path = Path
    .of("modules", "example", "target", "native-image", "example.exe")
    .toAbsolutePath

  val outputDir: Path =
    Path.of("modules", "e2e", "target", "e2e-output").toAbsolutePath

  if (!Files.exists(Utils.outputDir))
    Files.createDirectory(outputDir)

  require(Files.exists(Utils.executable))
  require(Files.exists(Utils.outputDir))
}
