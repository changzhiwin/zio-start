# 原理
> Runtimes bundle a thread pool together with the environment that effects need.
运行环境核心是两个部分：依赖树 和 线程池。

# 什么是Runtime System
我们写ZIO的程序时，都是在用ZIO的构造函数创建一些列的数据结构，描述怎么做一件事情（blueprint），而不是实际做这件事。
那么是谁让这个事情真正运行起来了？当然，它就是Runtime System。

# Runtime System的主要事务
- 执行ZIO构造函数创建的blueprint
- 处理所有异常情况
- 创建fiber，支持并发
- 协同fiber，每个fiber得到公平的资源
- 追踪、捕获程序运行栈，让调试错误的时候，得到更多更好的详细报告
- 保障资源不泄露，各种finalizers有序执行
- 处理异步回调，ZIO的世界里可以把所有回调透明化

# 渐进集成ZIO到老项目的方法
ZIO自带有一个最小、可用的Runtime.default，用于直接运行effect。
```
import zio._

object RunZIOEffectUsingUnsafeRun extends scala.App {
  val myAppLogic = for {
    _ <- Console.printLine("Hello! What is your name?")
    n <- Console.readLine
    _ <- Console.printLine("Hello, " + n + ", good to meet you!")
  } yield ()

  Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(
        myAppLogic
      ).getOrThrowFiberFailure()
  }
}
```

# 两种类型的Runtime
- Top-level，整个ZIO应用只有一个
- Locally scoped，满足不同执行片段，配置不同的Runtime
这里是ZLayer发挥无限可能的地方。

# Locally Scoped Runtime
> In ZIO all runtime configurations are inherited from their parent workflows.
由于底层是继承关系，所有配置`Local Runtime`属于`override`，超出配置的`scope`就会还原为父亲的配置。

## ZIO#provide(X, Y, Z)
```
import zio._

object MainApp extends ZIOAppDefault {
  val addSimpleLogger: ZLayer[Any, Nothing, Unit] =
    Runtime.addLogger((_, _, _, message: () => Any, _, _, _, _) => println(message()))

  def run =
    for {
      _ <- ZIO.log("Application started!")
      _ <- {
        for {
          _ <- ZIO.log("I'm not going to be logged!")
          _ <- ZIO.log("I will be logged by the simple logger.").provide(addSimpleLogger)
          _ <- ZIO.log("Reset back to the previous configuration, so I won't be logged.")
        } yield ()
      }.provide(Runtime.removeDefaultLoggers)
      _ <- ZIO.log("Application is about to exit!")
    } yield ()
}
```

## override ZIOApp.bootstrap
原理：ZIO框架在初始化好`top-level`Runtime后，会把`bootstrap`作为ZLayer，`provide`给`run`方法。
理所当然的，这种方式`scope`是`run`里面的effect。
```
// 相当于下面的行为是内置的
object MainApp extends ZIOAppDefault {

  override val bootstrap: ZLayer[Any, Nothing, Unit] = ???

  def run = (???).provide(bootstrap)
}
```

# Top-level Runtime
ZIO的应用，一开始就会初始化一个顶级的Runtime，需要定制的部分通过上面提到的两种Local方式来完成。
一般而言，这种组织关系可以满足最大部分的应用需求；但也提供了自定义顶级Runtime的方法：
```
import zio._

object MainApp extends ZIOAppDefault {

  // In a real-world application we might need to implement a `sl4jlogger` layer
  val addSimpleLogger: ZLayer[Any, Nothing, Unit] =
    Runtime.addLogger((_, _, _, message: () => Any, _, _, _, _) => println(message()))

  val layer: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers ++ addSimpleLogger

  // 覆盖了默认的行为
  override val runtime: Runtime[Any] =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.unsafe.fromLayer(layer)
    }

  def run = ZIO.log("Application started!")
}
```
**值得提醒**：Runtime实例是唯一可以直接使用`unsafe`方法来`run`effect数据结构的。

# 改变Runtime的ZEnvironment
ZEnvironment是承载**依赖**关系的，对于应用而言，大部分时候需要提供不同的依赖关系，例如在测试环境中，很多服务接口都使用mock。
## 先来看看Runtime.default的实现
都是用`empty`来占位的
```
object Runtime {
  val default: Runtime[Any] =
    Runtime(ZEnvironment.empty, FiberRefs.empty, RuntimeFlags.default)
}
```
## 方法一：直接新建一个Runtime
把我们需要注册的`Service`直接初始化到`ZEnvironment`里面去。
```
val testableRuntime = Runtime(
  ZEnvironment[LoggingService, EmailService](LoggingServiceLive(), EmailServiceFake()),
  FiberRefs.empty,
  RuntimeFlags.default
)
```
## 方法二：在默认基础上添加
框架提供了接口可以添加`Service`，但要注意类型参数的正确写法。
```
val testableRuntime: Runtime[LoggingService with EmailService] =
  Runtime.default.withEnvironment {
    ZEnvironment[LoggingService, EmailService](LoggingServiceLive(), EmailServiceFake())
  }
```

## 最后，运行
**特别注意**：这种运行方式不适用于`ZIOAppDefault.run`
```
Unsafe.unsafe { implicit unsafe =>
    testableRuntime.unsafe.run(
      for {
        _ <- LoggingService.log("sending newsletter")
        _ <- EmailService.send("David", "Hi! Here is today's newsletter.")
      } yield ()
    ).getOrThrowFiberFailure()
}
```

## 没有搞明白：为啥 override bootstrap 不行？
> 例子：TopRuntime.scala / RuntimeCustom.scala

怎么改写都不能在`ZIOAppDefault`里面运行成功，看看ZIO的源码实现:
```
// 2.x源码里面定义的main方法
private[zio] trait ZIOAppPlatformSpecific { self: ZIOApp =>

  /**
   * The Scala main function, intended to be called only by the Scala runtime.
   */
  final def main(args0: Array[String]): Unit = {
    implicit val trace  = Tracer.newTrace
    implicit val unsafe = Unsafe.unsafe

    val newLayer =
      Scope.default +!+ ZLayer.succeed(ZIOAppArgs(Chunk.fromIterable(args0))) >>>
        bootstrap +!+ ZLayer.environment[ZIOAppArgs with Scope]

    runtime.unsafe.fork {
      (for {
        runtime <- ZIO.runtime[Environment with ZIOAppArgs with Scope]
        _       <- installSignalHandlers(runtime)
        _       <- runtime.run(run).tapErrorCause(ZIO.logErrorCause(_))
      } yield ()).provideLayer(newLayer.tapErrorCause(ZIO.logErrorCause(_))).exitCode.tap(exit)
    }
  }
}
```