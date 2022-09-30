package zio.reference.experiment.di

import zio._

case class A(a: Int)
object A {
  val layer: ZLayer[Any, Nothing, A] =
    ZLayer.fromZIO {
      for {
        _ <- ZIO.debug("Initializing A")
        _ <- ZIO.sleep(3.seconds)
        _ <- ZIO.debug("Initialized A")
      } yield A(1)
    }
}

case class B(b: Int)
object B {
  val layer: ZLayer[Any, Nothing, B] =
    ZLayer.fromZIO {
      for {
        _ <- ZIO.debug("Initializing B")
        _ <- ZIO.sleep(2.seconds)
        _ <- ZIO.debug("Initialized B")
      } yield B(2)
    }
}

object ParallelLayer extends ZIOAppDefault {
  val myApp: ZIO[A with B, Nothing, Int] =
    for {
      a <- ZIO.serviceWith[A](_.a)
      b <- ZIO.serviceWith[B](_.b)
    } yield a + b

  def run =
    myApp
      .debug("result")
      .provide(A.layer, B.layer)
}