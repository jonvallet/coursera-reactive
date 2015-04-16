import org.scalacheck.Arbitrary._

val minimum = if (1 < 2) 1 else 2

val value1 = arbitrary[Int].sample.get
val value2 = arbitrary[Int].sample.get


val m = if (value1 < value2) value1 else value2