package zio.reference.experiment.contextual

import java.nio.charset.StandardCharsets
import zio._

/**
  * Notice: the default
  * val environment: ZEnvironment[Console & Clock & Random & System] =
  *   ZEnvironment[Console, Clock, Random, System](
  *     Console.ConsoleLive,
  *     Clock.ClockLive,
  *     Random.RandomLive,
  *     System.SystemLive
  *   )
  */

trait Database {
  def add(key: String, value: Array[Byte]): Task[Unit]
}

object Database {
  val layer: ULayer[Map[String, Database]] = {
    ZLayer.succeedEnvironment(
      ZEnvironment(
        Map(
          "persistent" -> PersistentDatabase(),
          "inmemory" -> InmemoryDatabase()
        )
      )
    )
  }
}

case class InmemoryDatabase() extends Database {
  override def add(key: String, value: Array[Byte]): Task[Unit] = {
    ZIO.unit <* ZIO.debug(s"new $key added to the inmemory, length = ${value.size}")
  }
}

case class PersistentDatabase() extends Database {
  override def add(key: String, value: Array[Byte]): Task[Unit] = {
    ZIO.unit <* ZIO.debug(s"new $key added to the persistent, length = ${value.size}")
  }
}
object MultiDatabase extends ZIOAppDefault {

  val myApp = for {
    inmemory   <- ZIO.serviceAt[Database]("inmemory")
                     .flatMap(x => ZIO.fromOption[Database](x))
                     .orElseFail("failed to find an in-memory database in the ZIO environment")
    persistent <- ZIO.serviceAt[Database]("persistent")
                     .flatMap(x => ZIO.fromOption[Database](x))
                     .orElseFail("failed to find an persistent database in the ZIO environment")
    _ <- inmemory.add("key1", "value1".getBytes(StandardCharsets.UTF_8))
    _ <- persistent.add("key2", "value2".getBytes(StandardCharsets.UTF_8))
  } yield ()

  def run = myApp.provideLayer(Database.layer)
}