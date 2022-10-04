# Why `UIO` and `URIO` have a `U` prefix
ZIO把错误分成两大类：可预期、不可预期。对应的`Effect`就有：可以产生错误的、不可产生错误的(不可预期)。
- Task, RIO: Exceptional Effect
- UIO, URIO: Unexceptional Effect
这里的`U`就是Unexceptional的意思。

# Typed Errors只是保证可预期的异常
> Typed errors don't guarantee the absence of defects and interruptions.

对于漏洞(defect)、打断(interruption)也是爱莫能助。
```
// 签名没有表征: NumberFormatException
def validateNonNegativeNumber(input: String): ZIO[Any, String, Int] =
  input.toIntOption match {
    case Some(value) if value >= 0 =>
      ZIO.succeed(value)
    case Some(other) =>
      ZIO.fail(s"the entered number is negative: $other")
    case None =>
      ZIO.die(
        new NumberFormatException(
          s"the entered input is not in the correct number format: $input"
        )
      )
  }
```

# 错误处理的一种哲学：Don't Type Unexpected Errros
> Let it crash is the erlang philosophy. It is a good philosophy for all unexpected errors.

总是觉得错误处理是一件很难搞的事情；当确实也不知道该怎么应对一种错误的时候，那么[crash](https://zio.dev/reference/error-management/best-practices/unexpected-errors)不失为好办法。


# 最佳实践
## 1, 相同领域(子领域)使用`sealed trait`来个建模Errors
```
sealed trait UserServiceError extends Exception

case class InvalidUserId(id: ID) extends UserServiceError
case class ExpiredAuth(id: ID)   extends UserServiceError
```

## 2, 遗失错误的类型，会使程序员产生本能的防御：打印日志
特别是调用其他API，文档很多时候没有详细描述所有可能产生的异常；这个时候很容易的处理措施就是，打印API的返回，捕获所有
异常。
```
upload("contacts.csv").catchAll {
  case FileExist(name) => delete("contacts.csv") *> upload("contacts.csv")
  case _ =>
    for {
      // 面对无类型的错误，典型但不佳的处理方式
      _ <- ZIO.log(error.toString) // logging the error
      _ <- ZIO.fail(error) // failing again (just like rethrowing exceptions in OOP)
    } yield ()
}
```
应该努力保持住错误的类型，防止丢失；有明确的错误类型信息，减少程序员的焦虑 和 不知所措。
```
val myApp: ZIO[Any, UploadError, Unit] =
  upload("contacts.csv")
    .catchSome {
      case FileExist(name) => delete(name) *> upload(name)
    }
```
