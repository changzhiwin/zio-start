package zio.http.experiment

import zio.http._
import zio.http.Path

object PathMatch {

  def main(args: Array[String]): Unit = {
    val pathStr = if (args.length == 1) args(0) else "/abc/123"

    val path = Path.decode(pathStr)

    println(s"path: ${path.toString}")
    println("-------------------------")

    path match {
      case !! / "abc" / int(id) => {
        println(s"No.1: Get int = $id")
      }
      
      case !! / "abc" / boolean(condition)  => {
        println(s"No.2: Get bool = $condition")
      }

      case !! / "abc" / double(num)  => {
        println(s"No.3: Get double = $num")
      }

      case "" /: "some" /: first /: second /: ~~ => {
        println(s"No.4: first = ${first}, second = ${second}")
      }

      case p: Path => {
        println(s"Default: $p")
      }
    }
  }
}