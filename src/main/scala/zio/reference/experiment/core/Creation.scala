package zio.reference.experiment.core

import zio._
import scala.util._
import scala.concurrent.Promise

object Creation extends ZIOAppDefault {

  // 1，fail后不会继续往后执行
  // 2，没有catcheAll，框架会在fail的地方打印error日志
  // 3，有catcheAll的情况，异常都归catchAll管了
  // 4, 可以 yield 因异常执行不到的值
  def failBreak = for {
    s <- ZIO.succeed(42)
    e <- ZIO.fail(new java.lang.Exception("Uh exception!"))
    _ <- ZIO.debug("I'm here")
    r <- ZIO.succeed(100)
  } yield r

  //def run = failBreak.debug("Return: ").catchAll {e => ZIO.debug(s"catch all: ${e}")}

  def func: String => String = s => s.toUpperCase

  def promiseT = for {
    promise <- ZIO.succeed(Promise[String]())
    _       <- ZIO.attempt {
      Try(func("zio")) match {
        case Success(value)     => promise.success(value)
        case Failure(exception) => promise.failure(exception)
      }
    }.fork
    value <- ZIO.fromPromiseScala(promise)
    _     <- Console.printLine(s"Hello, ${value}")
  } yield ()

  def run = for {
    s <- ZIO.suspend(ZIO.attempt(Console.printLine("Suspended Hello World!"))).debug("su: ")
    _ <- s.debug("s: ")
  } yield ()
}