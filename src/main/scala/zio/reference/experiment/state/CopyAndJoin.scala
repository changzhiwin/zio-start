package zio.reference.experiment.state

import zio._

object CopyAndJoin extends ZIOAppDefault {

  def copyOnFork = for {
    fiberRef <- FiberRef.make(3)
    promise <- Promise.make[Nothing, Int]
    _ <- fiberRef.updateAndGet(_ => 6).flatMap(r => promise.succeed(r)).fork
    childValue <- promise.await
    parentValue <- fiberRef.get
  } yield (childValue, parentValue)

  def customMerge = for {
    fiberRef <- FiberRef.make(initial = 0) // , join = math.max)
    child <- fiberRef.update(_ + 2).fork
    _ <- fiberRef.update(_ + 8)
    _ <- child.join
    value <- fiberRef.get
  } yield (value == 2)

  def run = copyOnFork.debug("Fork: ") *> customMerge.debug("Result: ")
}