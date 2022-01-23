package pl.andrzejressel.wormhole.unsafe

import cats.data.OptionT
import cats.effect._
import cats.effect.kernel.Sync
import cats.implicits._

import java.io.{FileInputStream, RandomAccessFile}
import java.nio.ByteBuffer
import java.nio.file.{Path => JPath}
import scala.io.Source

object File {
  def readFile[F[_]](path: JPath)(implicit S: Sync[F]): OptionT[F, String] = {
    val file = path.toFile
    val out  = S.delay {
      Source.fromInputStream(new FileInputStream(file))
    }
    val r    = Resource
      .fromAutoCloseable(out)
      .use(bs => S.blocking(bs.mkString))
      .attempt
      .map(logThrowable[F, String]("Cannot read file"))
      .flatMap(_.value)

    OptionT(r)
  }

  def writeToFile[F[_]](
    path: JPath
  )(text: String)(implicit F: Sync[F]): F[Unit] = {

    val fileResource    = Resource.fromAutoCloseable(
      F.delay(new RandomAccessFile(path.toFile, "rw"))
    )
    val channelResource = (file: RandomAccessFile) =>
      Resource.fromAutoCloseable(F.delay(file.getChannel))

    fileResource
      .flatMap(channelResource)
      .use { channel =>
        F.blocking {
          channel
            .truncate(0)
            .write(ByteBuffer.wrap(text.getBytes))
        }
      }
      .attempt
      .map(logThrowable[F, Int]("Cannot write to file"))
      .flatMap(_.value)
      .void
  }

}
