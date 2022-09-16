package zio.reference.experiment.concurrent

import zio._
import zio.Fiber.Status
import java.util.concurrent.TimeUnit

object FibonacciFiber extends ZIOAppDefault {

  def fibUseFork(n: Int): UIO[Int] = {
    n match {
      case m if m <= 1 => ZIO.succeed(1)
      case i => for {
                  - <- ZIO.sleep(500.milliseconds)
                  f2 <- fibUseFork(n - 2).fork
                  f1 <- fibUseFork(n - 1).fork
                  v1 <- f1.join
                  v2 <- f2.join 
                } yield (v1 + v2)
    }
  }

  def fibOnlyZIO(n: Int): UIO[Int] = {
    n match {
      case m if m <= 1 => ZIO.succeed(1)
      case i => for {
                  ft <- fibOnlyZIO(n - 1).zip(fibOnlyZIO(n - 2))
                } yield (ft._1 + ft._2)
    }
  }

  def monitorFibers(supervisor: Supervisor[Chunk[Fiber.Runtime[Any, Any]]]) = for {
    length <- supervisor.value.map(_.length)
    _ <- Console.printLine(s"number of fibers: $length")
  } yield()

  // 20 => 10946
  // 25 => 121393
  def run = for {
    supervisor <- Supervisor.track(true)
    st <- Clock.currentTime(TimeUnit.MILLISECONDS)
    fiber <- fibUseFork(20).supervised(supervisor).fork

    policy = Schedule
      .spaced(500.milliseconds)
      .whileInputZIO[Any, Unit](w => Console.printLine(s"what in schedule [${w}]").orDie *> fiber.status.map(_ != Status.Done))

    logger <- monitorFibers(supervisor)
      .repeat(policy).fork

    _ <- logger.join
    result <- fiber.join

    ed <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _ <- Console.printLine(s"fibonacci result: ${result}, cost ${ed - st}")
  } yield ()
}