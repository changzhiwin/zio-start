package zio.reference.experiment.core

import zio._

object BlockOperation extends ZIOAppDefault {

  def blockTask(n: Int): UIO[Unit] = {
    // 随机分派给线程执行
    // TODO，不知道zio的策略，main的线程池有多大？起100个task还是都能执行到
    Console.printLine(s"Running task $n" + s", on [${Thread.currentThread().getId()}]").orDie *>
      ZIO.succeed(Thread.sleep(10000)) *>
      blockTask(n)
  }

  def attemptBlockTask(n: Int) = ZIO.attemptBlockingInterrupt {
    // 一直会在同一个线程里执行
    while(true) {
      println(s"Running attempt task $n" + s", on [${Thread.currentThread().getId()}]")
      Thread.sleep(3000)
    }
  }

  def run = ZIO.foreachPar( (1 to 100).toArray ) {i => blockTask(i)}
}