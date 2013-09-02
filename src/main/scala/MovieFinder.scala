import twitter4j.{TwitterFactory,Query}
import scala.collection.JavaConverters._
import org.apache.commons.io.FileUtils

/**
 * Connect to Twitter, find 72 hours worth of #IMDb tweets with movie ratings
 */
object MovieFinder {
  
  val twitter = new TwitterFactory().getInstance
  val threeDaysAgo = new java.util.Date().getTime - (3 * 86400 * 1000)
  val Pattern = ".*rated (.*) ([^\\s-]{1,2})/10 (http://t.co/.*) #IMDb.*".r
  
  def extractMovie(tweet: String): (String, String, String) = tweet match {
    case Pattern(title, rating, url) => (title, rating, url)
    case _ => ("", "", "")
  }
  
  def getMoreTweets(texts: List[String], maxId: Long): List[String] = {
    val query = new Query("I rated #imdb")
    query.setCount(100)
    query.setMaxId(maxId)
    val tweets = twitter.search(query).getTweets.asScala.toList
    val oldestDate = (tweets.map(_.getCreatedAt.getTime)).min
    val oldestId = (tweets.map(_.getId)).min
    val newTexts = texts ++ tweets.map(_.getText)
    println("got " + tweets.size + " tweets")
    if (oldestDate > threeDaysAgo) getMoreTweets(newTexts, oldestId - 1)
    else newTexts
  }
  
  def main(args: Array[String]) { 
    val outfile = if (args.length == 0) "movies.html" else args(0)
    println("will write to: " + outfile)
    val tweets = getMoreTweets(List(), -1)
    val movies = tweets.map(extractMovie).filter(_._1 != "")
    //rearrange the movies into a usable structure, sort them by number of ratings, keep the most popular
    val avg: List[Float] => Float = (x => x.sum / x.length)
    val top20 = movies.groupBy(_._1).map(mv => 
      (mv._2.length, mv._1, avg(mv._2.map(x => x._2.toFloat)), mv._2(0)._3)).toList.sortBy(- _._1).take(20) 
      
      val page = <html><title>20 most rated movies on IMDb in the past 72 hours</title>
      <body><h2>20 most rated movies on IMDb in the past 72 hours</h2>
      <p>generated on { new java.util.Date().toString }</p><ul>
      { top20.map(movie => <li><a href={movie._4}>{movie._2}</a> - {movie._1.toString} ratings - average score: {"%.2f".format(movie._3)}</li>) }
      </ul></body></html>
        
      FileUtils.writeStringToFile(new java.io.File(outfile), page.toString)
    }
  }