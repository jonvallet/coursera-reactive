package calculator

object TweetLength {
  final val MaxTweetLength = 140
  final val MaxGreen = (15, "green")
  final val MaxOrange = (0,"orange")
  final val MaxRed = "red"

  def tweetRemainingCharsCount(tweetText: Signal[String]): Signal[Int] = {
    Signal(MaxTweetLength-tweetLength(tweetText()))
  }

  def colorForRemainingCharsCount(remainingCharsCount: Signal[Int]): Signal[String] = {

    def getColor(n: Int): String = n match {
      case n if n >= MaxGreen._1 => MaxGreen._2
      case n if n >= MaxOrange._1 => MaxOrange._2
      case _ => MaxRed
    }
    val signal = Var(getColor(remainingCharsCount()))
    signal
  }

  /** Computes the length of a tweet, given its text string.
   *  This is not equivalent to text.length, as tweet lengths count the number
   *  of Unicode *code points* in the string.
   *  Note that this is still a simplified view of the reality. Full details
   *  can be found at
   *  https://dev.twitter.com/overview/api/counting-characters
   */
  private def tweetLength(text: String): Int = {
    /* This should be simply text.codePointCount(0, text.length), but it
     * is not implemented in Scala.js 0.6.2.
     */
    if (text.isEmpty) 0
    else {
      text.length - text.init.zip(text.tail).count(
          (Character.isSurrogatePair _).tupled)
    }
  }
}
