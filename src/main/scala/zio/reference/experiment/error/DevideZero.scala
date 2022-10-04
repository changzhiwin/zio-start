package zio.reference.experiment.error

import java.io.IOException
import zio._

// retryUntil 接受的参数是异常
// repeatUntil 接受的参数是值
object DevideZero extends ZIOAppDefault {

  val myApp = ( for {
    a <- readNumber("Enter the first number (a):")
    b <- readNumber("Enter the second number (b):").repeatUntil(_ != 0)
    r <- devide(a , b)
    _ <- Console.printLine(s"a / b = $r")
  } yield (r) ).repeatUntil(_ == 9).onInterrupt( Console.printLine("End of process.").orDie)

  def run = for {
    f <- myApp.fork
    _ <- f.interrupt.delay(10.seconds)
    _ <- f.join
  } yield ()

  def parseInput(input: String): ZIO[Any, NumberFormatException, Int] = 
    ZIO.attempt(input.toInt).refineToOrDie[NumberFormatException]

  def readNumber(msg: String): ZIO[Any, IOException, Int] = 
    ( Console.print(msg) *> Console.readLine.flatMap(parseInput(_)) ) // zio.ZIO[Any,Exception,Int]
      .retryUntil(!_.isInstanceOf[NumberFormatException])
      .refineToOrDie[IOException]                                     // zio.ZIO[Any,java.io.IOException,Int]

  def devide(a: Int, b: Int): ZIO[Any, Nothing, Int] = ZIO.succeed(a / b) 
}