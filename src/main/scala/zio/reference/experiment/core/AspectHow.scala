package zio.reference.experiment.core

import zio._

/**
  * 核心理念是为了更好的模块化程序逻辑
  * 一般而言，程序逻辑分为：核心逻辑 和 切入逻辑
  * 核心逻辑关心what
  * 切入逻辑关心how
  */

object AspectHow extends ZIOAppDefault {

  def randomFail(label: String): ZIO[Any, Exception, String] = for {
    r1 <- Random.nextBoolean
    r2 <- Random.nextBoolean
    rr <- if (r1 == r2) ZIO.succeed(s"[${label}] ok") else ZIO.fail(new Exception(s"[${label}] sorry"))
  } yield rr

  def run = ZIO.foreachPar(List("zio", "akka")) { label =>
    randomFail(label) @@
      ZIOAspect.debug @@
      ZIOAspect.retry(Schedule.fibonacci(1.seconds)) @@
      ZIOAspect.loggedWith[String](msg => s"random: ${msg}")
  }
  
}