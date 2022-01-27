package pl.andrzejressel.wormhole.usecase

import cats.effect.IO
import pl.andrzejressel.wormhole.service.ConfigGenerator
import pl.andrzejressel.wormhole.terminal.Terminal

case class GenerateConfigUseCase() {

  def run(terminal: Terminal): IO[Unit] = {
    val generator = ConfigGenerator.generate(terminal)
    IO.println(generator.text)
  }

}
