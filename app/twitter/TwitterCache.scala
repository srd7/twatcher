package twatcher.twitter

import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.oauth.RequestToken

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

object TwitterCache {
  private def getKey(url: String, token: RequestToken) = token.token + " - " + url
  def get(url: String, token: RequestToken): Option[Any] =
    Cache.get(getKey(url, token))

  def getAs[T: ClassTag](url: String, token: RequestToken): Option[T] =
    Cache.getAs[T](getKey(url, token))

  def remove(url: String, token: RequestToken): Unit =
    Cache.remove(getKey(url, token))

  def set(url: String, token: RequestToken, value: Any, expiration: Duration): Unit =
    Cache.set(getKey(url, token), value, expiration)

  def set(url: String, token: RequestToken, value: Any, expiration: Int = 0): Unit =
    Cache.set(getKey(url, token), value, expiration)
}
