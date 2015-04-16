package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h))==m
  }

  lazy val genHeap: Gen[H] = for {
    n <- arbitrary[Int]
    heap <- oneOf(const(insert(n, empty)), genHeap)
  } yield heap

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

}
