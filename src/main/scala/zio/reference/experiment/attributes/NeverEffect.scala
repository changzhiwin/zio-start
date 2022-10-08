package zio.reference.experiment.attributes

import zio._

object NeverEffect extends ZIOAppDefault {

  val echo = Console.printLine("I'm here.") *> ZIO.never

  def run = for {
    _ <- Console.printLine("First")
    _ <- echo                        // ZIO.never moral equivalent of while(true) {}
    _ <- Console.printLine("Last")   // No chance to execute
  } yield ()
}