package zio.reference.experiment.concurrent

import zio._

object ZipFork extends ZIOAppDefault {
  
  def zipApp = for {
    // Inherit envrionment from parent
    fiber <- (
               ZIO.sleep(3.seconds) *> 
               Console.printLine("Hello, after 3 seconds") *>
               ZIO.succeed(33)
             ).fork
    _ <- Console.printLine("Hello, World!")
    res <- fiber.join
    _ <- Console.printLine(s"Out fiber succeed with $res")
  } yield ()

  def randomFail = for {
    b <- Random.nextBoolean
    fiber <- (if (b) ZIO.succeed(10) else ZIO.fail("The boolean was not true")).fork
    exitValue <- fiber.await
    _ <- exitValue match {
          case Exit.Success(value) => Console.printLine(s"Fiber succeed with $value")
          case Exit.Failure(cause) => Console.printLine(s"Fiber failed")
        }
  } yield ()

  def run = randomFail.repeat(Schedule.recurs(5))
}