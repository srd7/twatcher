package twatcher.twitter

import java.util.Date

/**
 * Error of Twitter API Response
 */
case class TwitterError(
  message: String
, code   : Int
) {
  def toException(parent: Throwable) = new TwitterException(message, code, parent)
  def toException: TwitterException = toException(null)
}

/**
 * Object of User
 */
case class User(
  twitterUserId            : Long
, name                     : String
, screenName               : String
, location                 : String
, discription              : String
, protectedValue           : Boolean
, followersCount           : Long
, friendsCount             : Long
, createdAt                : Date
, favouritesCount          : Long
, statusesCount            : Long
, profileBackgroundColor   : String
, profileBackgroundImageUrl: String
, profileImageUrl          : String
, profileLinkColor         : String
, profileTextColor         : String
)

/**
 * Object of single Tweet
 */
case class Tweet(
  createdAt          : Date
, id                 : Long
, text               : String
, source             : String
, inReplyToScreenName: Option[String]
, inReplyToStatusId  : Option[Long]
, inReplyToUserId    : Option[Long]
, user               : User
)

