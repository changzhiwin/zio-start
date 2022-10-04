package zio.reference.experiment.testing

import java.io.IOException
import zio._

object HelloWorld {

  def sayHello: ZIO[Any, IOException, Unit] =
    Console.printLine("Hello, ZIO!")
}