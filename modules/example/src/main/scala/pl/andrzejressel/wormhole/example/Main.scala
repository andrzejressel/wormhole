package pl.andrzejressel.wormhole.example

import pl.andrzejressel.wormhole.EntryPoint
import pl.andrzejressel.wormhole.model.AnsiColor._
import pl.andrzejressel.wormhole.model.Config
import pl.andrzejressel.wormhole.module.{CurrentDirectory, CurrentTimeModule}

object Main
    extends EntryPoint(
      Config(
        modules = Seq(
          CurrentDirectory(White, Black),
          CurrentTimeModule(White, Black)
        )
      )
    )
