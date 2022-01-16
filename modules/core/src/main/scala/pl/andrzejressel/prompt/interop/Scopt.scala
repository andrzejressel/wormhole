package pl.andrzejressel.prompt.interop

import enumeratum._
import scopt.Read
import scopt.Read.reads

object Scopt {

  def scoptRead[A <: EnumEntry](e: Enum[A]): Read[A] =
    reads {
      e.withNameInsensitive
    }

}
