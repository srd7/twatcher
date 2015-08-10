package twatcher.models

import slick.driver.H2Driver.api._

import play.api.libs.oauth.RequestToken

case class Account (
  userId: Long
, screenName: String
, imageUrl: String
, accessToken: String
, accessTokenSecret: String
, goodbyeFlag: Boolean = false
, tweetDeleteFlag: Boolean = false
, favoriteDeleteFlag: Boolean = false
, updateProfile: Option[String] = None
) {
  def token = RequestToken(accessToken, accessTokenSecret)
}

class Accounts(tag: Tag) extends Table[Account](tag, "ACCOUNT") {
  def userId = column[Long]("USER_ID", O.PrimaryKey)
  def screenName = column[String]("SCREEN_NAME")
  def imageUrl = column[String]("IMAGE_URL")
  def accessToken = column[String]("ACCESS_TOKEN")
  def accessTokenSecret = column[String]("ACCESS_TOKEN_SECRET")
  def goodbyeFlag = column[Boolean]("GOODBYE_FLAG")
  def tweetDeleteFlag = column[Boolean]("TWEET_DELETE_FLAG")
  def favoriteDeleteFlag = column[Boolean]("FAVORITE_DELETE_FLAG")
  def updateProfile = column[Option[String]]("UPDATE_PROFILE")
  def * = (
    userId
  , screenName
  , imageUrl
  , accessToken
  , accessTokenSecret
  , goodbyeFlag
  , tweetDeleteFlag
  , favoriteDeleteFlag
  , updateProfile
  ) <> (Account.tupled, Account.unapply)
}

object Accounts extends TableQuery(new Accounts(_)) {
  def get = this.result
  def upsert(account: Account) = this.insertOrUpdate(account)
  def delete(userId: Long) = this.filter(_.userId === userId).delete
  def findByUserId(userId: Long) = this.filter(_.userId === userId).result.headOption
}
