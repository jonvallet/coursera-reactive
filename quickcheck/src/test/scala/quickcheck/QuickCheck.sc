import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import Gen._
val minimum = if (1 < 2) 1 else 2
val value1 = choose(0,100000).sample
val value2 = choose(0,100000)
//val m = if (value1 < value2) value1 else value2