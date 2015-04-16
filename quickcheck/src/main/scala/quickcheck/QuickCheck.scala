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

  property("min two elements") = forAll { (value1: Int, value2: Int) =>
    val m = if (value1 < value2) value1 else value2

    val heap = insert(value2,insert(value1,empty))
    findMin(heap) == m
  }

  property("delete 1 element heap should return empty") = forAll { (value1: Int) =>
    val heap = insert(value1, empty)
    isEmpty(deleteMin(heap))
  }

  property("sorted list") = forAll { (list: List[Int]) =>

    def load(list: List[Int]): H = list match {
      case Nil => empty
      case xs :: tail => insert(xs, load(tail))
    }

    def check(list:List[Int], heap: H): Boolean = {
      if (list.isEmpty)
        true
      else {
        val min = findMin(heap)
        if (min == list.head)
          check(list.tail, deleteMin(heap))
        else
          false
      }
    }

    val heap = load(list)
    val sortedList = list.sorted
    check(sortedList, heap)

  }

  lazy val genHeap: Gen[H] = for {
    n <- arbitrary[Int]
    heap <- oneOf(const(insert(n, empty)), genHeap)
  } yield heap

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

}
