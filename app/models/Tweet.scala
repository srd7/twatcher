package twatcher.models

import slick.driver.H2Driver.api._

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
  def get(userId: Long) = this.filter(_.userId === userId).result
  def latest(userId: Long) = this.filter(_.userId === userId).sortBy(_.tweetId.desc).result.headOption
}
