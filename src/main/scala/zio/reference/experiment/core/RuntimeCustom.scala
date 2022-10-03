package zio.reference.experiment.core

// 会初始化2遍: bootstrap/layer 分别一遍
// 多次尝试后，还是没有弄明白，bootstrap里面的实例为啥没有提升到run的环境里面
// Ask at stackoverflow
// https://stackoverflow.com/questions/73915303/why-override-bootstrap-not-work-for-run-method/73919987#73919987

// package example

import zio._
trait EmailService {
  def send(user: String, content: String): Task[Unit]
}
object EmailService {
  def send(user: String, content: String): ZIO[EmailService, Throwable, Unit] =
    ZIO.serviceWithZIO[EmailService](_.send(user, content))

  val live: ZLayer[Any, Nothing, EmailService] = 
    ZLayer.fromZIO( ZIO.succeed(EmailServiceFake()) <* Console.printLine("Init EmailService") ).orDie
}

case class EmailServiceFake() extends EmailService {
  override def send(user: String, content: String): Task[Unit] =
    Console.printLine(s"sending email to $user")
}

// Don't use ZIOAppDefault
object RuntimeCustom extends ZIOApp {

  override val bootstrap = EmailService.live

  override type Environment = EmailService

  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]
  
  def run = (for {
    _ <- ZIO.debug("Start...")
    _ <- EmailService.send("God", "Hi")
    _ <- ZIO.debug("End...")
  } yield ())

  // This can works, have no doubt
  //.provide(EmailService.live)
}