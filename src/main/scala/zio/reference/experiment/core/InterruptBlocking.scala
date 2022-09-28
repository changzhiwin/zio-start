package zio.reference.experiment.core

import zio._
import java.util.concurrent.atomic.AtomicReference

final case class BlockingService() {
  private val released = new AtomicReference(false)

  def start(): Unit = {
    while (!released.get()) {
      println("Doing some blocking operation")
      try Thread.sleep(2000)
      catch {
        case _: InterruptedException => ( println("Get InterruptedException...") ) // Swallowing InterruptedException
      }
    }
    println("Blocking operation closed.")
  }

  def close(): Unit = {
    println("Releasing resources and ready to be closed.")
    released.getAndSet(true)
  }
}

/**
  * 两条建议：
  * 1，对于阻塞型IO操作，建议用 ZIO.attepmtBlockingIO，对应IOException
  * 2，需要自定义打断的，建议用 ZIO.attemptBlockingCancelable
  */

object InterruptBlocking extends ZIOAppDefault {

  def run = for {
    service <- ZIO.attempt(BlockingService())
    //ZIO.attemptBlockingInterrupt {
    fiber   <- ZIO.attemptBlockingCancelable{
      service.start()
    } {
      ZIO.succeed(service.close())
    }.fork
    _       <- fiber.interrupt.schedule(Schedule.delayed(Schedule.duration(5.seconds)))

  } yield ()

}

// 另外一个典型例子：
// ZIO.attemptBlockingCancelable(ss.accept())(ZIO.succeed(ss.close()))