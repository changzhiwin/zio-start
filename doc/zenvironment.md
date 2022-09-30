# ZEnvironment
本质是一个`Map`: type => instance，key是类型，value是这个类型的实例。
```
// ZEnvironment#toString 的输出

ZEnvironment(
  Map(
    Console -> (zio.Console$ConsoleLive$@76a3e297, 0),
    Clock   -> (zio.Clock$ClockLive$@4d3167f4, 1), 
    Random  -> (RandomScala(scala.util.Random$@4eb7f003), 2), 
    System  -> (zio.System$SystemLive$@eafc191, 3)
  )
)
```


# 同一类型，多个实例，怎么办？
解决方法就是嵌套一层`Map`，通过**类型 + key**来查找
```
import zio._

case class AppConfig(host: String, port: Int)

object AppConfig {
  val layer: ULayer[Map[String, AppConfig]] =
    ZLayer.succeedEnvironment(
      ZEnvironment(
        Map(
          "prod" -> AppConfig("production.myapp", 80),
          "dev" -> AppConfig("development.myapp", 8080)
        )
      )
    )
}

// 使用
config <- ZIO.serviceAt[AppConfig]("dev")
```

# ZLayer的构造函数
- ZLayer.succeed
- ZLayer.scoped
- ZLayer.apply/ZLayer.fromZIO
- ZLayer.fromFunction