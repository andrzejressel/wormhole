package pl.andrzejressel.prompt.module

import cats.effect.Concurrent
import fs2.concurrent.Topic
import pl.andrzejressel.prompt.model.ConsoleState

import java.nio.file.Path

class DataSourceImpl[F[_]](
  private val topic: fs2.concurrent.Topic[F, ConsoleState]
) extends DataSource[F] {

  override def currentPath: fs2.Stream[F, Option[Path]] =
    topic.subscribe(1).map(_.currentDirectory)

}

object DataSourceImpl {
  def apply[F[_]: Concurrent](
    consoleStateStream: fs2.Stream[F, ConsoleState]
  ): fs2.Stream[F, DataSource[F]] =
    fs2.Stream.eval(Topic[F, ConsoleState]).flatMap { topic =>
      fs2.Stream(new DataSourceImpl[F](topic))

//      fs2
//        .Stream(List.fill(n)(topic.subscribe(1)))
//        .concurrently(
//          topic.subscribers.find(_ == n) >> topic.publish(src)
//        )
    }

//    fs2.concurrent.Topic[F, ConsoleState].flatMap {
//
//    }

//    fs2.Stream.eval(fs2.concurrent.Topic[F, ConsoleState])

  // for {
//    topic <- fs2.concurrent.Topic[F, ConsoleState]
//  } yield new DataSourceImpl(topic)
}
