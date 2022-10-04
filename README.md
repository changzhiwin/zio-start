# 用途
学习[ZIO官网](https://zio.dev/reference/)时做的练习。

# Core Part

## 创建ZIO的方法
- ZIO.fail
- ZIO.succeed
- ZIO.attempt
- ZIO.refineOrDie
- ZIO.blocking / ZIO.attemptBlocking / ZIO.attemptBlockingCancelable / ZIO.attemptBlockingIO
- ZIO.async / ZIO.asyncZIO
- ZIO.fromOption / ZIO.some / ZIO.none / ZIO.getOrFail /ZIO#some / ZIO#unsome / ZIO#option
- ZIO.fromEither /ZIO#either
- ZIO.formTry
- ZIO.fromFuture
- ZIO.fromPromiseScala
- ZIO.fromFiber / ZIO.fromFiberZIO
- ZIO.suspend / ZIO.suspendSucceed 推迟执行，返回的是ZIO类型，而不是普通的值

`succeed`/`attempt`/`refineOrDie`都可以包裹同步非阻塞代码逻辑，区别在于对异常的处理：不抛出异常、任意异常、指定类型的异常。

## 流程控制
- ZIO.when / ZIO.whenZIO / ZIO#when / ZIO#whenZIO 输出是Option
- ZIO.unless / ZIO#unless
- ZIO#ifZIO(onTrue = ???, onFalse = ???)
- ZIO.loop / ZIO.loopDiscard
- ZIO.iterate(init: A)(cond: Boolean)(body: A)
- ZIO.foreach / ZIO.foreachDiscard

## 阻塞型代码逻辑
- ZIO.attemptBlockingIO 处理阻塞型IO，用这个
- ZIO.attemptBlockingCancelable 处理中断逻辑，用这个
ZIO的执行引擎会启用专门的线程池，来运行这样的逻辑；目的是保护主逻辑的执行，永远不要处于饥饿状态。

## 常规操作
- ZIO#map
- ZIO#tap
- ZIO#flatMap
- ZIO.zip / zipLeft / zioRight / *> / <*
- ZIO#race 二选一
- ZIO#timeout(3.seconds)

## 并发执行
- ZIO#zioPar
- ZIO.foreachPar
- ZIO.reduceAllPar
这种计算需要满足`结合律`和`交换律`。对于任意一个子计算失败，整个并行任务都失败；如果希望忽略部分失败，需要转换成`ZIO[R, Nothing, A]`这种类型。
可以借助`ZIO#option` 或者 `ZIO#either`来提升。

## 错误处理
- ZIO#either ZIO[R, E, A] => ZIO[R, Nothing, Either[E, A]]
- ZIO#option ZIO[R, E, A] => ZIO[R, Nothing, Option[A]]，E会被忽略
- ZIO#catchAll / ZIO#catchSome 
- ZIO#orElse / ZIO#orElseFail
- ZIO#fold(failure: E => B, success: A => B) / ZIO#foldZIO 同时处理E和A
- ZIO#retry(schedule) / ZIO#retryN(n: Int) / ZIO#retryOrElse(schedule, orElse) / ZIO#retryUntil

## 资源泄露保护
- ZIO#ensuring 算是比较底层API，少用
- ZIO.acquireReleaseWith(open)(close)(use)


## ZIO Aspect
> A cross-cutting concern is more about how we do something than what we are doing
- ZIOAspect.debug
- ZIOAspect.retry
- ZIOAspect.loggedWith
```
import zio._

def download(url: String): ZIO[Any, Throwable, Chunk[Byte]] = ZIO.succeed(???)

ZIO.foreachPar(List("zio.dev", "google.com")) { url =>
  download(url) @@
    ZIOAspect.retry(Schedule.fibonacci(1.seconds)) @@
    ZIOAspect.loggedWith[Chunk[Byte]](file => s"Downloaded $url file with size of ${file.length} bytes")
}
```

# Discover

## ZIO#map / ZIO#flatMap
- map[B](f: (A) => B)(implicit): ZIO[R, E, B]
- flatMap[R1 <: R, E1 >: E, B](k: (A) => ZIO[R1, E1, B])(implicit): ZIO[R1, E1, B]
**Notice**: The difference argment of `f` and `k`.