package pl.andrzejressel.wormhole.terminal

import enumeratum._
import pl.andrzejressel.wormhole.interop.Scopt._
import scopt.Read

sealed trait Terminal extends EnumEntry {
  def escapeColor(color: String): String = color
  val configFileLocation: String
}

object Terminal extends Enum[Terminal] {
  val values: IndexedSeq[Terminal] = findValues

  implicit val terminalRead: Read[Terminal] = Terminal.scoptRead

  case object PowerShell extends Terminal {
    override val configFileLocation: String = "/terminal/powershell.ps1"
  }

  case object Zsh extends Terminal {
    // https://stackoverflow.com/questions/14049870/zsh-rprompt-weird-spacing
    override def escapeColor(color: String): String = f"%%{$color%%}"

    override val configFileLocation: String = "/terminal/zsh.zshrc"
  }

}
