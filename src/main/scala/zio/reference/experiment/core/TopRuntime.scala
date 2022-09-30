package zio.reference.experiment.core

import zio._

case class Foobar(name: String)

object TopRuntime extends ZIOAppDefault {
  val addSimpleLogger: ZLayer[Any, Nothing, Unit] =
    Runtime.addLogger((_, _, _, message: () => Any, _, _, _, _) => println(message()))
  
  //val customLive: ZLayer[Any, Nothing, Foobar] =
  //  ZLayer.fromZIO( ZIO.succeed(Foobar("Why me")) )

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers ++ addSimpleLogger

  def run =
    (for {
      _ <- ZIO.log("Application started!")
      _ <- ZIO.log("Application is about to exit!")
      foobar <- ZIO.service[Foobar]
      _ <- ZIO.log(s"Foobar: ${foobar.name}")
    } yield () ).provideEnvironment(
      ZEnvironment[Foobar](Foobar("Why not?"))
    )
    //.provide(customLive)
}