package zio.reference.experiment.streaming

import zio._
import zio.stream._
import scala.io.Source

object CountLines extends ZIOAppDefault {

  val fileName = "./build.sbt"

  def run = {
    val lines: ZStream[Any, java.lang.Throwable, String] =
      ZStream
        .acquireReleaseWith(
          ZIO.attempt(Source.fromFile(fileName)) <* Console.printLine("The file was opened.")
        )(
          x => ZIO.succeed(x.close()) <* Console.printLine("The file was closed.").orDie
        )
        .flatMap( is => ZStream.fromIterator(is.getLines()))

    /*
      ZStream.fromIteratorZIO(
        ZIO.attempt(Source.fromFile(fileName).getLines())
      )
    */
    //lines.foreach(Console.printLine(_))

    val countPipeline = ZPipeline.map[String, Int](s => 1)

    lines.via(countPipeline).run(ZSink.sum).flatMap { sum =>
      Console.printLine(s"[${fileName}] have ${sum} lines")
    }
  }
}