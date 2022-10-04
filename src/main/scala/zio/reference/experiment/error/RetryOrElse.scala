package zio.reference.experiment.error

import zio._

object RetryOrElse extends ZIOAppDefault {
  def run =
    Random
      .nextIntBounded(11)
      .flatMap { n =>
        if (n < 9)
          ZIO.fail(s"$n is less than 9!").debug("failed")
        else
          ZIO.succeed(n).debug("succeeded")
      }
      .retryOrElse(
        policy = Schedule.recurs(5),
        orElse = (lastError, scheduleOutput: Long) =>
          ZIO.debug(s"after $scheduleOutput retries, we couldn't succeed!") *>
            ZIO.debug(s"the last error message we received was: $lastError") *>
            ZIO.succeed(-1)
      )
      .debug("the final result")
}