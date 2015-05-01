import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._

val s = "Hello"
val f: Future[String] = Future {
  s + " future!"
}
f onSuccess {
  case msg => println(msg)
}

val f2: Future[Int] = Future {2}

val n = Await.result(f2, 2 seconds)

val f3 = Future(3)

Await.result(f3, 0 seconds)
val duration = 2 seconds
def op(n: Int, f: Int => Int) = f(5)

op(1, {a =>a + 2})