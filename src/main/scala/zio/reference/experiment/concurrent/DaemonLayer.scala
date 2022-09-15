package zio.reference.experiment.concurrent

import zio._

object DaemonLayer extends ZIOAppDefault {

  // bind to global scope
  val layer: ZLayer[Scope, Nothing, Int] = 
    ZLayer.fromZIO {
      ZIO
        .debug("Still running...")
        .repeat(Schedule.fixed(1.second))
        .forkDaemon
        .as(12)
    }

  def run = for {
    _ <- ZIO.service[Int].provideLayer(layer) *> ZIO.debug("Int layer provided")
    _ <- ZIO.sleep(5.seconds)
  } yield ()
  
 }