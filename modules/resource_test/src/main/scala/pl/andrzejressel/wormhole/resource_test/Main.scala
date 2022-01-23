package pl.andrzejressel.wormhole.resource_test

import pl.andrzejressel.wormhole.terminal.Terminal

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {

    Terminal.values.foreach(terminal =>
      Source
        .fromInputStream(getClass.getResourceAsStream(terminal.getConfigFile()))
        .mkString
    )

    println("Resource test ended successfully")

  }
}
