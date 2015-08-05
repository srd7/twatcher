package twatcher.models

import slick.driver.H2Driver.api._

import play.api.libs.oauth.RequestToken

case class Account (
  screenName: String
, accessToken: String
, accessTokenSecret: String
) {
  def token = RequestToken(accessToken, accessTokenSecret)
}

class Accounts(tag: Tag) extends Table[Account](tag, "ACCOUNT") {
  def screenName = column[String]("SCREEN_NAME")
  def accessToken = column[String]("ACCESS_TOKEN")
  def accessTokenSecret = column[String]("ACCESS_TOKEN_SECRET")
  def * = (screenName, accessToken, accessTokenSecret) <> (Account.tupled, Account.unapply)
}

object Accounts extends TableQuery(new Accounts(_)) {
  def initial = this.result
}
