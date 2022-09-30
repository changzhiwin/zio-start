# 理解
> The ZLayer data types act as a dependency/environment eliminator. By providing required dependencies to our ZIO application, ZLayer eliminates all dependencies from the environment of our application.

`ZLayer`扮演一个依赖消者的角色，可以把`R`变成`Any`，例如下面的`A with B` => `Any`。
```
// Sequential
val myApp: ZIO[A with B, Nothing, (String, Int)] =
  for {
    a <- A.foo
    b <- B.bar
  } yield (a, b)

// Parallel
val myApp: ZIO[A with B, Nothing, (String, Int)] = A.foo <&> B.bar

val appLayer: ZLayer[Any, Nothing, A with B] = 
  A.layer ++ B.layer

val result: ZIO[Any, Nothing, (String, Int)] = myApp.provideLayer(appLayer)

```

# 构建依赖图
- 手动构建，使用`++`（水平）和`>>>`（垂直）两个操作符
- 自动构建，零件一起扔进去，让ZIO去组装，使用`ZIO#provide` 或者 `ZIO#provideSome`

## 注入依赖的方式
- 如果是手动构建的Layer，使用`ZIO#provideLayer` 或者 `ZIO#provideSomeLayer`
- 对于自动构建的情况，直接使用`ZIO#provide` 或者 `ZIO#provideSome`
从命名其实可以理解两者的差别，自动构建没有`Layer`的意思；而手动构建的就是`Layer`。

## 手动构建操作符
- `++`: A with B
- `>>>`: A ==> B
- `>+>`: A ==> A with B，专业术语`passthrough`

`ZLayer`对于垂直构建采取的默认策略是隐藏上一层的依赖。`passthrough`也有非操作符的写法：
```
import zio._

val fooLayer: ZLayer[A, Throwable, B] = ???     // A  ==> B
val barLayer: ZLayer[B, Throwable, C] = ???     // B  ==> C

val finalLayer: ZLayer[A, Throwable, B & C] =   // A ==> B & C
  fooLayer >+> barLayer                         // 方法一
  fooLayer >>> (ZLayer.service[B] ++ barLayer)  // 方法二
  fooLayer >>> barLayer.passthrough             // 方法三
```

# 对Layer部分进行更新、替换
## update进行更新
```
import zio._
import java.io.IOException

case class AppConfig(poolSize: Int)

object MainApp extends ZIOAppDefault {

  val myApp: ZIO[AppConfig, IOException, Unit] =
    for {
      config <- ZIO.service[AppConfig]
      _ <- Console.printLine(s"Application config after the update operation: $config")
    } yield ()

  val appLayers: ZLayer[Any, Nothing, AppConfig] =
    ZLayer(ZIO.succeed(AppConfig(5)).debug("Application config initialized"))

  val updatedConfig: ZLayer[Any, Nothing, AppConfig] =
    appLayers.update[AppConfig](c =>
      c.copy(poolSize = c.poolSize + 10)
    )

  def run = myApp.provide(updatedConfig)
}
```
## 使用水平组合操作符`++`进行替换
```
import zio._

val origin: ZLayer[Any, Nothing, String & Int & Double] =
  ZLayer.succeedEnvironment(ZEnvironment[String, Int, Double]("foo", 123, 1.3))

val updated = origin ++ ZLayer.succeed(321)
```

# 对比一下手动构建和自动构建的写法
> 最佳实践：对于单个Layer的命名用`live`，对于组合的命名用`layer`。

## 手动构建，用provideLayer
```
object MainApp extends ZIOAppDefault {

  val layers: ULayer[Cake] =
      (((Spoon.live >>> Chocolate.live) ++ (Spoon.live >>> Flour.live)) >>> Cake.live)

  def run = myApp.provideLayer(layers)

}
```
## 自动构建，用provide
```
object MainApp extends ZIOAppDefault {
  def run =
    myApp.provide(
      Cake.live,
      Chocolate.live,
      Flour.live,
      Spoon.live  
    )
}
```

# 如何使用自动构建出Layer
小范围使用自动构建机制，可以灵活的得到，自定义的顶层Layer。
## ZLayer.make[R]
```
val chocolateAndFlourLayer: ZLayer[Any, Nothing, Chocolate & Flour] =
  ZLayer.make[Chocolate & Flour](
    Chocolate.live,
    Flour.live,
    Spoon.live
  )
```
## ZLayer.makeSome[R0, R]
只提供一部分的依赖解析，没解析的继续保留在`R`里。
```
val cakeLayer: ZLayer[Spoon, Nothing, Cake] =
  ZLayer.makeSome[Spoon, Cake](
    Cake.live,
    Chocolate.live,
    Flour.live
  )
```

## 调试ZLayer
打印依赖树。
```
import zio._

object MainApp extends ZIOAppDefault {
  def run =
    myApp.provide(
      Cake.live,
      Chocolate.live,
      Flour.live,
      Spoon.live,
      ZLayer.Debug.tree 
      // or
      // ZLayer.Debug.mermaid
    )
}
```

# ZLayer维护的实例
ZLayer默认全局使用单例模式：依赖树上多次出现的某个实例引用，只会产生一个实例。

## 需要使用不同实例，ZLayer#fresh
```
object MainApp extends ZIOAppDefault {

  val myApp: ZIO[B & C, Nothing, Unit] =
    for {
      _ <- ZIO.service[B]
      _ <- ZIO.service[C]
    } yield ()

  def run = myApp.provideLayer((a.fresh >>> b) ++ (a.fresh >>> c))
}
```
## 局部provide时，不产生不同实例，不会共享
```
object MainApp extends ZIOAppDefault {

  val myApp: ZIO[Any, Nothing, Unit] =
    for {
      _ <- ZIO.service[A].provide(a) // 新建A
      _ <- ZIO.service[A].provide(a) // 新建A
    } yield ()

  def run = myApp
}
```

## 当然也可以手动使其共享
```
  val myApp: ZIO[Any, Nothing, Unit] =
    ZIO.scoped {
      a.memoize.flatMap { aLayer =>
        for {
          _ <- ZIO.service[A].provide(aLayer)
          _ <- ZIO.service[A].provide(aLayer)
        } yield ()
      }
    }
```