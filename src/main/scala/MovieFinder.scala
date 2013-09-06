import twitter4j.{TwitterFactory,Query}
import scala.collection.JavaConverters._
import org.apache.commons.io.FileUtils

/**
 * Connect to Twitter, find 72 hours worth of #IMDb tweets with movie ratings
 */
object MovieFinder {

  val twitter = new TwitterFactory().getInstance
  val threeDaysAgo = new java.util.Date().getTime - (3 * 86400 * 1000)
  val Pattern = ".*rated (.*) ([0-9]{1,2})/10 (http://t.co/.*) #IMDb.*".r
  val pageTitle ="20 most rated movies on IMDb in the past 72 hours"

  def extractMovie(tweet: String): (String, String, String) = tweet match {
    case Pattern(title, rating, url) => (title, rating, url)
    case _ => ("", "", "")
  }

  //keep searching for tweets until we find some older than three days
  def getTweets(texts: List[String], maxId: Long): List[String] = {
    val query = new Query("I rated #imdb")
    query.setCount(100)
    query.setMaxId(maxId)
    val tweets = twitter.search(query).getTweets.asScala.toList
    val oldestDate = (tweets.map(_.getCreatedAt.getTime)).min
    val oldestId = (tweets.map(_.getId)).min
    val newTexts = texts ++ tweets.map(_.getText)
    println("got " + newTexts.size + " tweets")
    //usually we get about 2k tweets, make 5k the limit to avoid hitting rate limits
    if (oldestDate > threeDaysAgo && newTexts.length < 5000) getTweets(newTexts, oldestId - 1)
    else newTexts
  }

  // Render a list of (Ratings, Title, Average Score, Url)
  def writeHTML(movies: List[(Int, String, Float, String)], outfile: String) {
    val now = new java.text.SimpleDateFormat("HH:mm:ss z, dd-MMM-yyyy").format(new java.util.Date())
    val page = <html><style type="text/css">body {{font-family: verdana, arial, helvetica}}
      a:link {{text-decoration:none;}}
      tr:nth-child(even) {{background: #EEE}}
      tr:nth-child(odd) {{background: #FFF}}</style>
      <title>{ pageTitle }</title><body><h2>{ pageTitle }</h2>
        <p>generated at { now }</p>
        <table border="1" cellspacing="0" cellpadding="2">
          <tr><th>Title</th><th>Ratings</th><th>Avg. score</th></tr>
          { movies.map(m => <tr><td><a href={m._4}>{m._2}</a></td>
          <td>{m._1.toString}</td><td>{"%.1f".format(m._3)}</td></tr>) }
        </table><br/>Source code at <a href="http://github.com/dbasch/moviefinder">github.com/dbasch/moviefinder</a>.</body></html>

    FileUtils.writeStringToFile(new java.io.File(outfile), page.toString)
  }

  def main(args: Array[String]) {
    val outfile = if (args.length == 0) "movies.html" else args(0)
    println("will write to: " + outfile)
    val tweets = getTweets(List(), -1)
    val ratings = tweets.map(extractMovie).filter(_._1 != "")
    val avg: List[Float] => Float = (x => x.sum / x.length)
    //group the ratings by movie title
    val movies = ratings.groupBy(_._1).map(mv => (mv._2.length, mv._1, avg(mv._2.map(x => x._2.toFloat)), mv._2(0)._3))
    //sort them by number of ratings, keep the most popular, render web page
    writeHTML(movies.toList.sortBy(- _._1).take(20), outfile)
  }
}
