package zio.reference.experiment.attributes

import zio._

object GetRuntime extends ZIOAppDefault {

  val program = for {
    _ <- Console.printLine("Something occur.")
    //_ <- ZIO.fail("Some error")
    i <- Random.nextInt
  } yield i

  def run = for {
    rt <- ZIO.runtime[Any]
    re <- ZIO.succeed {
      Unsafe.unsafe { implicit unsafe =>
        rt.unsafe.run(program)
      }
    }
    _  <- re match {
      case Exit.Success(value) =>
        Console.printLine(s"Result: $value")
      case Exit.Failure(cause) =>
        Console.printLine(s"Exited: $cause")
    }
  } yield ()
}