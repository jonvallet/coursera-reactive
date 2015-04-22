package calculator

import scala.math._

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double],
      c: Signal[Double]): Signal[Double] = {
    val delta = Var(pow(b(),2) - 4*(a() * c()))
    delta
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
      c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = {

    def getRoots(a: Double, b: Double, c: Double, delta: Double): Set[Double] =
    if (delta < 0) {
      Set()
    }else {
      val firstRoot = (-b + sqrt(delta)) / (2 * a)
      val secondRoot = (-b - sqrt(delta)) / (2 * a)
      Set(firstRoot, secondRoot)
    }

    val roots = Var(getRoots(a(),b(),c(),delta()))
    roots
  }
}
