package pl.andrzejressel.workhole.example

import pl.andrzejressel.prompt.EntryPoint
import pl.andrzejressel.prompt.model.AnsiColor._
import pl.andrzejressel.prompt.model.Config
import pl.andrzejressel.prompt.module.{CurrentDirectory, CurrentTimeModule}

object Main
    extends EntryPoint(
      Config(modules =
        Seq(
          CurrentDirectory(White, Black),
          CurrentTimeModule(White, Black)
        )
      )
    )
