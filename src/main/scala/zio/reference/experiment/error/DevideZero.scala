package zio.reference.experiment.error

import java.io.IOException
import zio._

// retryUntil 接受的参数是异常
// repeatUntil 接受的参数是值
object DevideZero extends ZIOAppDefault {

  def run = ( for {
    a <- readNumber("Enter the first number (a):")
    b <- readNumber("Enter the second number (b):").repeatUntil(_ != 0)
    r <- devide(a , b)
    _ <- Console.printLine(s"a / b = $r")
  } yield (r) ).repeatUntil(_ == 3)

  def parseInput(input: String): ZIO[Any, NumberFormatException, Int] = 
    ZIO.attempt(input.toInt).refineToOrDie[NumberFormatException]

  def readNumber(msg: String): ZIO[Any, IOException, Int] = 
    ( Console.print(msg) *> Console.readLine.flatMap(parseInput(_)) )
      .retryUntil(!_.isInstanceOf[NumberFormatException])
      .refineToOrDie[IOException]

  def devide(a: Int, b: Int): ZIO[Any, Nothing, Int] = ZIO.succeed(a / b) 
}