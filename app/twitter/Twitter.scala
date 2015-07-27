package twatcher.twitter

import twatcher.twitter.TwitterUris._

import play.api.libs.oauth.{ConsumerKey, OAuth, ServiceInfo}

class Twitter(key: String, secret: String) extends TwitterApi {
  override protected[this] val consumerKey = ConsumerKey(key, secret)

  // oauth key to authenticate App for users
  val oauth = OAuth(
    ServiceInfo(OAUTH_REQUEST_TOKEN, OAUTH_ACCESS_TOKEN, OAUTH_AUTHORIZE, consumerKey)
  , false
  )
}
