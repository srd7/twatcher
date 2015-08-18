package twatcher.models

import slick.driver.H2Driver.api._
import slick.jdbc.GetResult

case class Tweet (
  userId : Long
, tweetId: Long
)

class Tweets(tag: Tag) extends Table[Tweet](tag, "TWEET") {
  def userId = column[Long]("USER_ID")
  def tweetId = column[Long]("TWEET_ID")
  def * = (userId, tweetId) <> (Tweet.tupled, Tweet.unapply)
}

object Tweets extends TableQuery(new Tweets(_)) {

  def insertAll(tweets: Seq[Tweet]) = DBIO.seq(this ++= tweets)
  def get(userId: Long) = this.filter(_.userId === userId).result
  def count: DBIO[Seq[(Long, Int)]] = {
    sql"SELECT USER_ID, COUNT(1) FROM TWEET GROUP BY (USER_ID)".as[(Long, Int)]
  }
  def latest(userId: Long) = this.filter(_.userId === userId).sortBy(_.tweetId.desc).result.headOption
}
