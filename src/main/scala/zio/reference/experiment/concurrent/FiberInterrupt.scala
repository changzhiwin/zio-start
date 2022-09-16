package zio.reference.experiment.concurrent

import zio._

object FiberInterrupt extends ZIOAppDefault {

  def ensure = for {
    fiber <- Console.printLine("Working on the first job")
                    .schedule(Schedule.fixed(1.seconds))
                    .ensuring {
                      // ZIO[Any,IOException,Unit] -> ZIO[Any,Nothing,Unit]
                      (Console.printLine("Finalizing time cousuming") *> ZIO.sleep(7.seconds)).orDie
                    }
                    .fork
    // Case 1: interrupt will block 7 seconds, until ensuring finished.
    //_ <- fiber.interrupt.delay(4.seconds)

    // Case 2: fast interrupt, no block
    _ <- fiber.interruptFork.delay(4.seconds)

    _ <- Console.printLine("Main task over.")
  } yield ()

  // TODO: don't understand why deadlock?
  def joinInterruption = (
    for {
      fiber <- Console.printLine("Running a job").schedule(Schedule.fixed(1.seconds)).fork
      _ <- fiber.interrupt.delay(5.seconds)
      _ <- fiber.join
    } yield ()
  ).ensuring(
    Console.printLine("This finalizer will be executed without occurring any deadlock").orDie
  )

  def run = joinInterruption
}