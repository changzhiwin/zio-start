package zio.reference.experiment.state

import zio._

case class Logging private (ref: FiberRef[Map[String, String]]) {

  def logAnnotate[R, E, A](key: String, value: String)(zio: ZIO[R, E, A]): ZIO[R, E, A] = {
    ref.locallyWith(_ +  (key -> value))(zio)
  }

  def log(message: String): UIO[Unit] = {
    ref.get.flatMap {
      case annotate if annotate.isEmpty => 
        ZIO.succeed(message) //Console.printLine(message).orDie
      case annotate =>
        val line = s"${annotate.map { case (k, v) => s"[$k=$v]"}.mkString(" ")} $message"
        ZIO.succeed(line)
    }.flatMap(m => Console.printLine(m).orDie)
  }
}

object Logging {
  def make() = FiberRef.make(Map.empty[String, String]).map(new Logging(_))
}