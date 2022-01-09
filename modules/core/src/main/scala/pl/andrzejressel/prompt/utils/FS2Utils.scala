package pl.andrzejressel.prompt.utils

import cats.effect.implicits._
import cats.effect.{Concurrent, Deferred, Ref}
import cats.implicits._
import fs2.Pipe
import fs2.concurrent.Topic

object FS2Utils {

  def shareAndCombine[F[_]: Concurrent, A, B](
    pipes: Seq[fs2.Pipe[F, A, Option[B]]]
  ): fs2.Pipe[F, A, Seq[Option[B]]] = { src =>
    src
      .through(shareN(pipes.size))
      .map(
        _.mapWithIndex((a, b) => a.through(pipes(b)).map(cs => b -> cs))
      )
      .flatMap(fs2.Stream.emits)
      .parJoinUnbounded
      .scan(Seq.fill[Option[B]](pipes.size)(None)) { (a, b) =>
        a.updated(b._1, b._2)
      }

  }

  private def shareN[F[_]: Concurrent, A](
    n: Int
  ): fs2.Pipe[F, A, List[fs2.Stream[F, A]]] = { src =>
    fs2.Stream.eval(Topic[F, A]).flatMap { topic =>
      fs2
        .Stream(List.fill(n)(topic.subscribe(1)))
        .concurrently(
          topic.subscribers.find(_ == n) >> topic.publish(src)
        )
    }

  }

  def prefetchKeepLatest[F[_]: Concurrent, A](): Pipe[F, A, A] =
    prefetchKeepLatest(_ => ().pure[F])

  def prefetchKeepLatest[F[_]: Concurrent, A](
    onDiscard: A => F[Unit]
  ): Pipe[F, A, A] = { in =>
    sealed trait State[B]
    case class Empty[B]()                         extends State[B]
    case class Full[B](value: B)                  extends State[B]
    case class Waiting[B](reader: Deferred[F, B]) extends State[B]

    val initial = Ref.of[F, State[Option[A]]](Empty())

    fs2.Stream.eval(initial).flatMap { q =>
      def enqueue(v: Option[A]): F[Unit] =
        q.modify {
          case Empty()         => Full(v)            -> ().pure[F]
          case Waiting(reader) => Empty[Option[A]]() -> reader.complete(v).void
          case Full(old)       => Full(v)            -> old.traverse_(onDiscard)
        }.flatten
          .uncancelable

      def dequeue = Deferred[F, Option[A]].flatMap { wait =>
        q.modify {
          case Empty()    => Waiting(wait)      -> wait.get
          case Full(v)    => Empty[Option[A]]() -> v.pure[F]
          case Waiting(_) =>
            throw new Exception("impossible: single consumer queue")
        }.flatten
      }

      val producer =
        in.map(Some(_))
          .evalMap(enqueue)
          .onFinalize(enqueue(None))

      val consumer =
        fs2.Stream.repeatEval(dequeue).unNoneTerminate

      consumer.concurrently(producer)
    }
  }

}
