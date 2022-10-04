package zio.reference.experiment.testing

import zio._
import zio.test._
import zio.test.Assertion._

// sbt> Test/testOnly zio.reference.experiment.testing.HelloWorldSpec
// sbt> Test/runMain zio.reference.experiment.testing.HelloWorldSpec

object HelloWorldSpec extends ZIOSpecDefault {

  def spec = suite("HelloWorldSpec")(
    test("sayHello correctly displays output") {
      for {
        _      <- HelloWorld.sayHello
        output <- TestConsole.output
      } yield assertTrue(output == Vector("Hello, ZIO!\n"))
    },

    test("updating ref") {
      for {
        r <- Ref.make(1)
        _ <- r.update(_ + 10)
        v <- r.get
      } yield assertTrue(v == 11)
    }
  )
}