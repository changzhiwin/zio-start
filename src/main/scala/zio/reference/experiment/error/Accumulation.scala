package zio.reference.experiment.error

import zio._

object Accumulation extends ZIOAppDefault {

  // 将E和A都收集成List，但是当错误发生，就会丢失所有的A: ZIO[R, ::[E], Collection[B]]
  val v2 = ZIO.validate(List.range(1, 7)) { n =>
    if (n < 10)
      ZIO.succeed(n)
    else
      ZIO.fail(s"$n is big one")
  }

  // 将E和A都收集成List，并且都成为A的一部分: ZIO[R, Nothing, (Iterable[E], Iterable[B])]
  val v3 = ZIO.partition(List.range(0, 7)) { n =>
    if (n % 2 == 0)
      ZIO.succeed(n)
    else 
      ZIO.fail(s"$n is not even")
  }

  def run = v3.debug

}