package zio.reference.experiment.state

import zio._

object FiberRefLogging extends ZIOAppDefault {
  def run = for {
    logging <- Logging.make()
    _ <- logging.log("Hello World!")
    _ <- ZIO.foreachParDiscard(List("Foo", "Bar")) { name =>
           logging.logAnnotate("name", name) {
             for {
               _ <- logging.log(s"Received request")

               fiberId <- ZIO.fiberId.map(_.ids.head)

               _ <- logging.logAnnotate("fiber_id", s"$fiberId") {
                 logging.log("Processing request")
               }

               _ <- logging.log("Finished processing request")
             } yield ()
           }
         }
    _ <- logging.log("All requests proceessed")
  } yield ()
}