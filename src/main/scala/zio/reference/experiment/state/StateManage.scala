package zio.reference.experiment.state

import zio._

object StateManage extends ZIOAppDefault {

  // Sequential, use recursive function
  def inputName: Task[List[String]] = {

    def loop(names: List[String]): Task[List[String]] = {
      Console.readLine("Please input name:").flatMap {
        case "q"    => ZIO.succeed(names)
        case name   => loop(names appended name)
      }
    }

    loop(Nil)
  }

  // Concurrent: use Ref
  def inputNameByRef: Task[List[String]] = {
    Ref.make(List.empty[String])
      .flatMap { ref =>
        Console
          .readLine("Please input name:")
          .orDie
          .repeatWhileZIO {
            case "q"  => ZIO.succeed(false)
            case name => ref.update(_ appended name).as(true)
          } *> ref.get
      }
  }

  def inputNameByFibers: Task[Seq[String]] = for {
    ref <- Ref.make(Seq.empty[String])
    f1  <- Console
             .readLine("Please input name:")
             .orDie
             .repeatWhileZIO {
               case "q"  => ZIO.succeed(false)
               case name => ref.update(name +: _).as(true)
             }
             .fork
    f2  <- ZIO.foreachDiscard(Seq("John", "Jane", "Joe", "Tom")) {  name =>
             ref.update(name +: _) *> ZIO.sleep(1.second)
           }.fork
    _   <- f1.join
    _   <- f2.join
    v   <- ref.get
  } yield v

  def run = inputNameByFibers.debug("Result:")
}