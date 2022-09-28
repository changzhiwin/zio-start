package zio.reference.experiment.core

import zio._

object ControlFlow extends ZIOAppDefault {

  def zioWhenT(i: Int) = Console.printLine(s"I'm here, count [$i]").whenZIO(Random.nextBoolean)

  def zioUntilT(i: Int) = Console.printLine(s"I'm until, count [$i]").unlessZIO(Random.nextBoolean)

  def loopDemo = (ZIO.loop(0)(_ < 10, _ + 1) { i => 

    // ZIO是一个数据结构，描述work flow，不会主动执行
    // 两种执行方式：
    // 1, 用for组织
    // 2, 用flatMap组织
    for {
      _ <- Console.printLine("start...")
      _ <- zioWhenT(i)
      _ <- Console.printLine("middle...")
      _ <- zioUntilT(i)
      _ <- Console.printLine("end...")
      tf <- Random.nextBoolean
    } yield tf //(Random.nextBoolean)
    
  }).debug("Loop: ")

  // iterate(初始值 A)(判断条件 A => Boolean) { body: A => ZIO[R, E, A]}
  def iteratorDemo = ZIO.iterate(0)(_ < 10) { i =>
    ZIO.succeed(i + 1).whenZIO(Random.nextBoolean).someOrElse(i).debug("Random: ")
  }.debug("iterate: ")

  def run = iteratorDemo
}