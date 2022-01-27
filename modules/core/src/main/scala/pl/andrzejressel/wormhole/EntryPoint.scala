package pl.andrzejressel.wormhole

import cats.effect._
import pl.andrzejressel.wormhole.model._
import pl.andrzejressel.wormhole.usecase.{
  GenerateConfigUseCase,
  StartApplicationUseCase
}
import scopt.OParser

abstract class EntryPoint(config: Config) extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    OParser.parse(CliConfig.parser, args, CliConfig()) match {
      case Some(value) =>
        (value.mode match {
          case s: CliMode.Start         =>
            StartApplicationUseCase().run(value.terminal, s, config)
          case CliMode.GenerateConfig() =>
            GenerateConfigUseCase().run(value.terminal)
        }).as(ExitCode.Success)
      case None        => IO(ExitCode.Error)
    }

  }

}
