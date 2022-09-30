package zio.reference.experiment.core

import zio._
import zio.internal.stacktracer.Tracer

trait LoggingService {
  def log(line: String): UIO[Unit]
}

object LoggingService {
  def log(line: String): URIO[LoggingService, Unit] =
    ZIO.serviceWith[LoggingService](_.log(line))

  val live: ZLayer[Any, Nothing, LoggingService] = ZLayer.succeed(LoggingServiceLive())
}

trait EmailService {
  def send(user: String, content: String): Task[Unit]
}

object EmailService {
  def send(user: String, content: String): ZIO[EmailService, Throwable, Unit] =
    ZIO.serviceWith[EmailService](_.send(user, content))

  val live: ZLayer[Any, Nothing, EmailService] = ZLayer.succeed(EmailServiceFake())
}

case class LoggingServiceLive() extends LoggingService {
  override def log(line: String): UIO[Unit] =
    Console.printLine(line).orDie
    //ZIO.succeed(print(line))
}

case class EmailServiceFake() extends EmailService {
  override def send(user: String, content: String): Task[Unit] =
    ZIO.log(s"sending email to $user")
    //Console.printLine(s"sending email to $user")
    //ZIO.attempt(println(s"sending email to $user"))
}

object RuntimeCustom { // extends ZIOAppDefault {

  //val layer = LoggingService.live ++ EmailService.live

  val runtime: Runtime[LoggingService with EmailService] =
    Runtime.default.withEnvironment {
    ZEnvironment[LoggingService, EmailService](LoggingServiceLive(), EmailServiceFake())
  }

  def main(args: Array[String]): Unit = {
    implicit val trace  = Tracer.newTrace
    implicit val unsafe = Unsafe.unsafe
    /*
    Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe.run(
          for {
            f1 <- LoggingService.log("sending newsletter").fork
            f2 <- EmailService.send("David", "Hi! Here is today's newsletter.").fork
            _ <- f1.zip(f2).join
            _ <- Console.printLine("Finished.")
          } yield ()
        ).getOrThrowFiberFailure()
    }
    */
    runtime.unsafe.run {
      (for {
        runtime <- ZIO.runtime[LoggingService with EmailService]
        _       <- runtime.run(run).tapErrorCause(ZIO.logErrorCause(_))
      } yield())
    }
  }

  def run = for {
    _ <- LoggingService.log("I am LoggingService")
    _ <- EmailService.send("God", "Hi")
  } yield ()

  //).provide(layer)
}
