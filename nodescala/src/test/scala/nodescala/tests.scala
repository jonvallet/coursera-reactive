package nodescala

import java.util.NoSuchElementException

import scala.language.postfixOps
import scala.util.{Try, Success, Failure}
import scala.collection._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.async.Async.{async, await}
import org.scalatest._
import NodeScala._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NodeScalaSuite extends FunSuite {

  test("A Future should always be completed") {
    val always = Future.always(517)

    assert(Await.result(always, 0 nanos) == 517)
  }

  test("A Future should never be completed") {
    val never = Future.never[Int]

    try {
      Await.result(never, 1 second)
      assert(false)
    } catch {
      case t: TimeoutException => // ok!
    }
  }

  test("Future.all(List(Future(10), Future(2)) should return Future(List(1,2))"){
    val result = Future.all(List(Future(1), Future(2)))

    assert(Await.result(result, 1 seconds) == List(1,2))
  }

  test("A any future should return the first future to complete") {
    val result = Future.any(List(Future {blocking(Thread.sleep(2000l)); 1}, Future {blocking(Thread.sleep(1000l)); 2}))

    assert(Await.result(result, 3 seconds) == 2)

  }

  test("CancellationTokenSource should allow stopping the computation") {
    val cts = CancellationTokenSource()
    val ct = cts.cancellationToken
    val p = Promise[String]()

    async {
      while (ct.nonCancelled) {
        // do work
      }

      p.success("done")
    }

    cts.unsubscribe()
    assert(Await.result(p.future, 1 second) == "done")
  }

  test("FutureOps(Future(1)).now() should return 1") {
    val future = FutureOps(Future(1))
    assert(future.now == 1)
  }

  test("FutureOps(Future.delay(1 second).now() should return NoSuchElementException") {
    try {
      val future = FutureOps(Future.delay(1 second))
      future.now
      fail()
    } catch {
      case t: NoSuchElementException => //ok
    }
  }

  test("FutureOps(Future(1)).continueWith(a:Future[Int] => Future(2)) should return Future(2)") {
    val f1: FutureOps[Int] = FutureOps(Future(1))
    val f2 = {a:Future[Int] => 2}
    val result = f1.continueWith(f2)

    assert (Await.result(result, 1 second) == 2)
  }

  test("FutureOps(Future(1)).continue(a:Future[Int] => Future(2)) should return Future(2)") {
    val f: FutureOps[Int] = FutureOps(Future(1))
    def f1(a: Try[Int]): Int = a match  {
      case Success(x) => x + 1
    }

    val result = f.continue(f1)

    assert (Await.result(result, 1 second) == 2)
  }

  class DummyExchange(val request: Request) extends Exchange {
    @volatile var response = ""
    val loaded = Promise[String]()
    def write(s: String) {
      response += s
    }
    def close() {
      loaded.success(response)
    }
  }

  class DummyListener(val port: Int, val relativePath: String) extends NodeScala.Listener {
    self =>

    @volatile private var started = false
    var handler: Exchange => Unit = null

    def createContext(h: Exchange => Unit) = this.synchronized {
      assert(started, "is server started?")
      handler = h
    }

    def removeContext() = this.synchronized {
      assert(started, "is server started?")
      handler = null
    }

    def start() = self.synchronized {
      started = true
      new Subscription {
        def unsubscribe() = self.synchronized {
          started = false
        }
      }
    }

    def emit(req: Request) = {
      val exchange = new DummyExchange(req)
      if (handler != null) handler(exchange)
      exchange
    }
  }

  class DummyServer(val port: Int) extends NodeScala {
    self =>
    val listeners = mutable.Map[String, DummyListener]()

    def createListener(relativePath: String) = {
      val l = new DummyListener(port, relativePath)
      listeners(relativePath) = l
      l
    }

    def emit(relativePath: String, req: Request) = this.synchronized {
      val l = listeners(relativePath)
      l.emit(req)
    }
  }
  test("Server should serve requests") {
    val dummy = new DummyServer(8191)
    val dummySubscription = dummy.start("/testDir") {
      request => for (kv <- request.iterator) yield (kv + "\n").toString
    }

    // wait until server is really installed
    Thread.sleep(500)

    def test(req: Request) {
      val webpage = dummy.emit("/testDir", req)
      val content = Await.result(webpage.loaded.future, 1 second)
      val expected = (for (kv <- req.iterator) yield (kv + "\n").toString).mkString
      assert(content == expected, s"'$content' vs. '$expected'")
    }

    test(immutable.Map("StrangeRequest" -> List("Does it work?")))
    test(immutable.Map("StrangeRequest" -> List("It works!")))
    test(immutable.Map("WorksForThree" -> List("Always works. Trust me.")))

    dummySubscription.unsubscribe()
  }

}




