package calculator

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalatest._

import TweetLength.MaxTweetLength

@RunWith(classOf[JUnitRunner])
class CalculatorSuite extends FunSuite with ShouldMatchers {

  /******************
   ** TWEET LENGTH **
   ******************/

  def tweetLength(text: String): Int =
    text.codePointCount(0, text.length)

  test("tweetRemainingCharsCount with a constant signal") {
    val result = TweetLength.tweetRemainingCharsCount(Var("hello world"))
    assert(result() == MaxTweetLength - tweetLength("hello world"))

    val tooLong = "foo" * 200
    val result2 = TweetLength.tweetRemainingCharsCount(Var(tooLong))
    assert(result2() == MaxTweetLength - tweetLength(tooLong))
  }

  test("tweetRemainingCharsCount with a supplementary char") {
    val result = TweetLength.tweetRemainingCharsCount(Var("foo blabla \uD83D\uDCA9 bar"))
    assert(result() == MaxTweetLength - tweetLength("foo blabla \uD83D\uDCA9 bar"))
  }


  test("colorForRemainingCharsCount with a constant signal") {
    val resultGreen1 = TweetLength.colorForRemainingCharsCount(Var(52))
    assert(resultGreen1() == "green")
    val resultGreen2 = TweetLength.colorForRemainingCharsCount(Var(15))
    assert(resultGreen2() == "green")

    val resultOrange1 = TweetLength.colorForRemainingCharsCount(Var(12))
    assert(resultOrange1() == "orange")
    val resultOrange2 = TweetLength.colorForRemainingCharsCount(Var(0))
    assert(resultOrange2() == "orange")

    val resultRed1 = TweetLength.colorForRemainingCharsCount(Var(-1))
    assert(resultRed1() == "red")
    val resultRed2 = TweetLength.colorForRemainingCharsCount(Var(-5))
    assert(resultRed2() == "red")
  }

  test("Calculator.eval(Plus(Literal(1.0), Literal(1.0)), Map()) should return 2.0") {
    Calculator.eval(Plus(Literal(1.0), Literal(1.0)), Map()) should be (2.0)
  }

  test("Calculator.eval(Plus(Literal(1.0), Ref(\"a\")), Map(\"a\"->Signal(Plus(Literal(2.0),Literal(3.0))))) should return 6.0") {
    Calculator.eval(Plus(Literal(1.0), Ref("a")), Map("a"->Signal(Plus(Literal(2.0),Literal(3.0))))) should be (6.0)
  }

  test("Calculator.computeValues(Map(\"a\"->Signal(Plus(Ref(\"b\"),Literal(3.0))),(\"b\"->Signal(Times(Literal(2),Ref(\"a\")))))) should return Map(\"a\"->Double.NaN") {
    val result = Calculator.computeValues(Map("a"->Signal(Plus(Ref("b"),Literal(3.0))),("b"->Signal(Times(Literal(2),Ref("a"))))))

    val signal: Signal[Double] = result.get("a").get
    assert(signal().isNaN)
  }

  test("Calculator.computeValues(Map(\"a\"->Signal(Plus(Literal(2.0),Literal(3.0))))) should return Map(\"a\"->5.0") {
    val result = Calculator.computeValues(Map("a"->Signal(Plus(Literal(2.0),Literal(3.0)))))

    val signal: Signal[Double] = result.get("a").get
    assert(signal() == 5.0)
  }

  test("Polynomial.computeDelta(Signal(1.0), Signal(1.0), Signal(1.0)) should return -3.0") {
    val actualResult = Polynomial.computeDelta(Signal(3.0), Signal(-2.0), Signal(-1.0))
    assert(actualResult() == 16.0)
  }

  test("Polynomial.computeSolutions") {
    val actualResult = Polynomial.computeSolutions(Signal(3.0), Signal(-2.0), Signal(-1.0), Signal(16.0))

    assert(actualResult() == Set(1.0, -1.0/3))
  }

}
