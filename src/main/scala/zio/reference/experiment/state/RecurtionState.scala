package zio.reference.experiment.state

import zio._

object RecurtionState extends ZIOAppDefault {

  def inputName: Task[List[String]] = {

    def loop(names: List[String]): Task[List[String]] = {
      Console.readLine("Please input name:").flatMap {
        case "q"    => ZIO.succeed(names)
        case name   => loop(names appended name)
      }
    }

    loop(Nil)
  }

  def run = inputName.debug("Result:")
}