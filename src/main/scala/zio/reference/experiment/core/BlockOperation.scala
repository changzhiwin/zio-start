package zio.reference.experiment.core

import zio._
import zio.Executor
import java.util.concurrent.{LinkedBlockingQueue, ThreadPoolExecutor, TimeUnit}

object BlockOperation extends ZIOAppDefault {

  override val bootstrap = Runtime.setExecutor(
    // 介绍参数：https://blog.csdn.net/FUTEROX/article/details/122893521
    Executor.fromThreadPoolExecutor(
      new ThreadPoolExecutor(
        5,
        10,
        5000,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue[Runnable]()
      )
    )
  )

  def blockTask(n: Int): UIO[Unit] = {
    // 随机分派给线程执行
    // 不知道zio的策略，main的线程池有多大？起100个task还是都能执行到
    // 通过自定义Runtime的Executor，可以方便的看出：确实阻塞了
    Console.printLine(s"Running task $n" + s", on [${Thread.currentThread().getId()}]").orDie *>
      ZIO.succeed(Thread.sleep(3000)) *>
      blockTask(n)
  }

  def attemptBlockTask(n: Int) = ZIO.attemptBlockingInterrupt {
    // 一直会在同一个线程里执行
    while(true) {
      println(s"Running attempt task $n" + s", on [${Thread.currentThread().getId()}]")
      Thread.sleep(3000)
    }
  }

  def run = ZIO.foreachPar( (1 to 8).toArray ) {i => blockTask(i)}
}