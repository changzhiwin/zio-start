package zio.reference.experiment.concurrent

import zio._

/**
  * Four type: auto-default | global | local scope | specific scope
  */
object FiberDefault extends ZIOAppDefault {
  
  var barJob: ZIO[Any, Nothing, Long] = ZIO.debug("Bar: still running!").repeat(Schedule.fixed(1.seconds))

  var fooJob: ZIO[Any, Nothing, Unit] = for {
    _ <- ZIO.debug("Foo: started!")
    _ <- barJob.onInterrupt(_ => ZIO.debug("Bar job interrupted!")).fork
    _ <- ZIO.sleep(3.seconds)
    _ <- ZIO.debug("Foo: fininshed!")
  } yield ()

  def run = for {
    f <- fooJob.fork
    _ <- f.join
  } yield ()
}