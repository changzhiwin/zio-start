# ZIO的三类错误
## Failures，一般性错误，应用开发者知道怎么处理这种错误
```
trait ZIO {
  def fail[E](error: => E): ZIO[Any, E, Nothing]
}
```
## Defects，系统缺陷，应用于开发者不希望看到，也不知道该怎么处理
> let it crash philosophy

注意看看返回类型的签名，输入和输出都是`Nothing`。
```
object ZIO {
  def die(t: => Throwable): ZIO[Any, Nothing, Nothing]
}

// 注意看succeed返回的也是 Nothing
scala> ZIO.succeed(3 / 0)
val res0: zio.ZIO[Any,Nothing,Int] = Sync(res0(<console>:1),$Lambda$4914/523912438@14a375cb)
```

## Fatal Errors, 致命错误
对于JVM级别的错误，ZIO是当成致命处理的，会打断整个应用，无法捕获。下面的代码，两个捕获都没用。
```
import zio._

object FatalError extends ZIOAppDefault {
  def run =
    ZIO
      .attempt(
        throw new StackOverflowError(
          "The call stack pointer exceeds the stack bound."
        )
      )
      .catchAll(_ => ZIO.unit)       // ignoring all expected errors
      .catchAllDefect(_ => ZIO.unit) // ignoring all unexpected errors
}
```