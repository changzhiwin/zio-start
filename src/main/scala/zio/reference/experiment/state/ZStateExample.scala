package zio.reference.experiment.state

import zio._

case class MyState(counter: Int)

/**
  * Notice:
  * ZState is that it is on top of the FiberRef data type. 
  * So it will inherit its behavior from the FiberRef
  */
object ZStateExample extends ZIOAppDefault {

  val myApp = for {
    _ <- ZIO.updateState[MyState](state => state.copy(counter = 2))
    state0 <- ZIO.getState[MyState]
    _ <- Console.printLine(s"Before fork the fiber: $state0")
    fiber <- 
      {
        for {
          _ <- ZIO.updateState[MyState](state => state.copy(counter = 6))
          state <- ZIO.getState[MyState]
          _ <- Console.printLine(s"Inside the forked fiber: $state")
        } yield ()
      }.fork
    _ <- ZIO.updateState[MyState](state => state.copy(counter = 13))
    state1 <- ZIO.getState[MyState]
    _ <- Console.printLine(s"Before merging the fiber: $state1")
    _ <- fiber.join
    state2 <- ZIO.getState[MyState]
    _ <- Console.printLine(s"After join the fiber: $state2")
  } yield ()

  def run = ZIO.stateful(MyState(0))(myApp)
}