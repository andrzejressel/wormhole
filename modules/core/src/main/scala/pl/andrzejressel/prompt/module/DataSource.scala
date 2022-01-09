package pl.andrzejressel.prompt.module

import java.nio.file.Path

trait DataSource[F[_]] {
  def currentPath: fs2.Stream[F, Option[Path]]
}
