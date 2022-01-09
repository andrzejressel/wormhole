package pl.andrzejressel.prompt.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Macros {

  def embedFile(path: String): Array[Byte] = macro embedFile_impl

  def embedFile_impl(
    c: blackbox.Context
  )(path: c.Expr[String]): c.universe.Tree = {
    import c.universe._

    path.tree match {
      case Literal(Constant(s: String)) =>
        Option(
          this.getClass.getResourceAsStream(s)
        ) match {
          case Some(stream) =>
            val arr = stream.readAllBytes()
            q"$arr"
          case None         => c.abort(c.enclosingPosition, "File does not exist")
        }
      case _                            => c.abort(c.enclosingPosition, "Need a literal path!")
    }
  }

}
