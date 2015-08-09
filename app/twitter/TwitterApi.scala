package twatcher.twitter

import twatcher.twitter.TwitterUris._
import twatcher.twitter.TwitterReads._

import play.api.libs.json.{JsValue, Reads}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.{WS, WSRequest}
import play.api.Play.current

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

sealed trait TwitterApiRepositoryComponent {
  val twitterApiRepository: TwitterApiRepository
  trait TwitterApiRepository {

    // consumer key of Twitter App
    protected[this] val ck: ConsumerKey

    /**
     * Set URL and sign with calculator
     */
    private[this] def buildRequest(url: String, token: RequestToken, param: (String, String)*): WSRequest = {
      WS.url(url)
        .sign(OAuthCalculator(ck, token))
        .withRequestTimeout(5.second.toMillis)
        .withQueryString(param: _*)
    }

    /**
     * Check whether error exists or not on the response JSON
     */
    private[this] def errorCheck(json: JsValue): Future[JsValue] = {
      (json \ "errors").asOpt[List[TwitterError]].fold(Future.successful(json))(eList =>
        Future.failed(eList.head.toException)
      )
    }

    /**
     * Generate raw URL of request
     */
    private[this] def getFullUrl(req: WSRequest): String = {
      val queryString = req.queryString
      val query = queryString.keys.flatMap{ key =>
        queryString(key) match {
          case values if values.length > 1 => {
            values.zipWithIndex.map { case (value, i) =>
              s"${key}[${i}]=$value"
            }
          }
          case value => {
            List(s"${key}=${value(0)}")
          }
        }
      }.mkString("&")
      req.url + "?" + query
    }

    /**
     * Send GET request to Twitter
     */
    def get[T : Reads](url: String, token: RequestToken, param: (String, String)*)
      (implicit ec: ExecutionContext): Future[T] = {
        val request = buildRequest(url, token, param: _*)
        val fullUrl = getFullUrl(request)
        TwitterCache.getAs[JsValue](fullUrl, token).fold(
          for {
            response  <- request.get()
            validated <- errorCheck(response.json)
            _         =  TwitterCache.set(fullUrl, token, validated, 15.minutes)
          } yield (validated.as[T])
        )(cached => Future.successful(cached.as[T]))
      }

    /**
     * Send POST request to Twitter
     */
    def post[T : Reads](url: String, token: RequestToken, param: (String, String)*)
      (implicit ec: ExecutionContext): Future[T] = {
        val request = buildRequest(url, token, param: _*)
        for {
          response  <- request.post("") // have to POST nothing
          validated <- errorCheck(response.json)
        } yield (validated.as[T])
      }
  }
}

sealed trait TwitterApiService { self: TwitterApiRepositoryComponent =>
  /**
   * POST Twitter to Twitter
   */
  def tweet(status: String, token: RequestToken)(implicit ec: ExecutionContext): Future[Tweet] =
    twitterApiRepository.post[Tweet](STATUSES_UPDATE, token, "status" -> status)

  /**
   * GET user timeline
   */
  def getTimeline(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[List[Tweet]] =
    twitterApiRepository.get[List[Tweet]](USER_TIMELINE, token, "user_id" -> userId.toString)

  def getTimeline(screenName: String, token: RequestToken)(implicit ec: ExecutionContext): Future[List[Tweet]] =
    twitterApiRepository.get[List[Tweet]](USER_TIMELINE, token, "screen_name" -> screenName)

  /**
   * GET user profile
   */
  def getProfile(token: RequestToken)(implicit ec: ExecutionContext): Future[User] = {
    twitterApiRepository.get[User](SELF_PROFILE, token)
  }

}

abstract class TwitterApi(ck: ConsumerKey) extends TwitterApiRepositoryComponent with TwitterApiService {
  protected[this] val consumerKey = ck
  val twitterApiRepository = new TwitterApiRepository {
    override protected[this] val ck = consumerKey
  }
}
