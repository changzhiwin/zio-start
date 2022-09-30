package zio.reference.experiment.error

import zio._

object FatalError extends ZIOAppDefault {
  def run =
    ZIO
      .attempt(
        throw new StackOverflowError(
          "The call stack pointer exceeds the stack bound."
        )
      )
      .catchAll(_ => ZIO.unit)       // ignoring all expected errors
      .catchAllDefect(_ => ZIO.unit) // ignoring all unexpected errors
}