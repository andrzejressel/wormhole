package pl.andrzejressel.wormhole.model

import pl.andrzejressel.wormhole.module.Module

case class Config(
  modules: Seq[Module]
)
