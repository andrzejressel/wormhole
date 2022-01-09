package pl.andrzejressel.prompt.resource_test

import pl.andrzejressel.prompt.terminal.Terminal

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {

    Terminal.values.foreach(terminal =>
      Source
        .fromInputStream(getClass.getResourceAsStream(terminal.getConfigFile()))
        .mkString
    )

  }
}
