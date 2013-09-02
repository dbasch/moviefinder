# MovieFinder

Find the top 20 most rated movies on [IMDb](http://imdb.com) in the past three days.

## Usage

Don't know what to watch? Do this:

1) create a file called twitter4j.properties with these contents:

    debug=false
    oauth.consumerKey=your key
    oauth.consumerSecret=your secret
    oauth.accessToken=your access token
    oauth.accessTokenSecret=your access token secret

If you don't have an app with credentials, get them [here](https://dev.twitter.com/apps/new).

2) Install [Scala Build Tool](http://www.scala-sbt.org/) if you don't have it.

3) From the command line, run:

    $ sbt run

This will connect to Twitter and fetch thousands of tweets of the form "I rated Some Movie 7/10 [link to the movie] on #IMDB ..." 
I'm assuming those tweets are a representative sample of all ratings. It will send the output to a file called movies.html.

Open movies.html with your browser. Pick a movie to watch, or maybe read a book instead.

## License

Copyright © 2013 Diego Basch

Distributed under the Eclipse Public License
