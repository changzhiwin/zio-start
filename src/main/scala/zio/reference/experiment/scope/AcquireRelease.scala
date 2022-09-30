package zio.reference.experiment.scope

import zio._
import scala.io.BufferedSource

case class AFile(name: String)

object AFile {
  val layer: ZLayer[Any, Nothing, AFile] = ZLayer.scoped {
    ZIO.acquireRelease( ZIO.debug("Open AFile") *> ZIO.succeed(AFile("foobar")))(
      file => ZIO.debug("Close AFile: " + file.name)
    )
  }

  val fileLayer: ZLayer[Any, Throwable, BufferedSource] =
    ZLayer.scoped {
      ZIO.fromAutoCloseable(
        ZIO.attempt(scala.io.Source.fromFile("file.txt"))
      )
    }
}

object AcquireRelease extends ZIOAppDefault {

  val myApp: ZIO[AFile, Nothing, String] = {
    ZIO.serviceWith[AFile](_.name).flatMap { name =>
      ZIO.succeed(s"Get - [$name]")
    }
  }

  def run = myApp.debug("result: ").provide(AFile.layer)

}

/*
Open AFile
result: : Get - [foobar]
Close AFile: foobar
[info] shutting down sbt server
*/