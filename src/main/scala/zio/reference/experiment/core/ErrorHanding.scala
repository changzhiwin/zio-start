package zio.reference.experiment.core

import zio._

object ErrorHanding extends ZIOAppDefault {

  // ZIO#either: Returns an effect whose failure and success have been lifted into an Either
  // ZIO#option: Executes this effect, skipping the error but returning optionally the success.

  // catchAll / catchSome 系列

  // fold / foldZIO / foldCause 系列，将 E/A 的情况一起考虑

  // retry / retryN / retryOrElse 系列

  def randomSome = for {
    // 发现 () 和 {} 存在语义上的差别点：当只有一个参数时 两者都能用，但有多个参数只能用()
    r <- ZIO.ifZIO(Random.nextBoolean)(
      onTrue = ZIO.succeed("good"),
      onFalse = ZIO.fail("bad")
    )
  } yield r

  def foldDemo: UIO[String] =  randomSome.fold(
    e => s"Error: $e",
    s => s"Success: $s"
  )

  def run = ZIO.foreachParDiscard((1 until 10).toSeq){ i => //ZIO.loop(0)(_ < 10, _ + 1) {
    foldDemo.flatMap { s =>
      Console.printLine(s"Print ${i}: ${s}")
    }
  }
}