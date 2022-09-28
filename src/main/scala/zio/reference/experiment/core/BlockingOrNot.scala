package zio.reference.experiment.core

import zio._
import scala.util._

object BlockingOrNot extends ZIOAppDefault {

  val blockDemo = for {
    _ <- ZIO.debug("start...")
    pass <- ZIO.attemptBlocking { // ZIO.attempt { //
      Thread.sleep(5000)
      "5 seconds pass"
    }.debug("Block: ").fork
    _ <- ZIO.debug("end...") // will waiting, when not fork
    _ <- pass.join
  } yield ()

  def run = blockDemo
}