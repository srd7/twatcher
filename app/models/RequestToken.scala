package twatcher.models

import play.api.libs.oauth.{RequestToken => OAuthToken}

case class RequestToken(
  screenName: String
, token     : String
, secret    : String
) {
  def toOAuthToken = OAuthToken(token, secret)
}
