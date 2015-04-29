import scala.concurrent._
import ExecutionContext.Implicits.global

val s = "Hello"
val f: Future[String] = Future {
  s + " future!"
}
f onSuccess {
  case msg => println(msg)
}
