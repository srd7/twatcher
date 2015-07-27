package twatcher.twitter

import twatcher.twitter.TwitterUris._
import twatcher.twitter.TwitterReads._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Reads}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.{WS, WSRequest}
import play.api.Play.current

import scala.concurrent.{ExecutionContext, Future}

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
        .withRequestTimeout(5000L) // milliseconds. TODO: to constant
        .withQueryString(param: _*)
    }

    /**
     * Check whether error exists or not on the response JSON
     */
    private[this] def errorCheck(json: JsValue): Future[JsValue] = {
      (json \ "error").asOpt[TwitterError].fold(Future.successful(json))(e => Future.failed(e.toException))
    }

    /**
     * Send GET request to Twitter
     */
    def get[T : Reads](url: String, token: RequestToken, param: (String, String)*): Future[T] = {
      val request = buildRequest(url, token, param: _*)
      for {
        response  <- request.get()
        validated <- errorCheck(response.json)
      } yield (validated.as[T])
    }

    /**
     * Send POST request to Twitter
     */
    def post[T : Reads](url: String, token: RequestToken, param: (String, String)*): Future[T] = {
      val request = buildRequest(url, token, param: _*)
      for {
        response  <- request.post("") // have to POST nothing
        validated <- errorCheck(response.json)
      } yield (validated.as[T])
    }
  }
}

sealed trait TwitterApiServiceComponent { self: TwitterApiRepositoryComponent =>
  val twitterApiService: TwitterApiService
  class TwitterApiService {

    /**
     * POST Twitter to Twitter
     */
    def tweet(status: String, token: RequestToken): Future[Tweet] =
      twitterApiRepository.post[Tweet](STATUSES_UPDATE, token, "status" -> status)

    /**
     * GET user timeline
     */
    def getTimeline(userId: Long, token: RequestToken): Future[List[Tweet]] =
      twitterApiRepository.get[List[Tweet]](USER_TIMELINE, token, "user_id" -> userId.toString)

    def getTimeline(screenName: String, token: RequestToken): Future[List[Tweet]] =
      twitterApiRepository.get[List[Tweet]](USER_TIMELINE, token, "screen_name" -> screenName)
  }
}

trait TwitterApi extends TwitterApiRepositoryComponent with TwitterApiServiceComponent {
  protected[this] val consumerKey: ConsumerKey
  val twitterApiRepository = new TwitterApiRepository {
    val ck = consumerKey
  }

  val twitterApiService    = new TwitterApiService
}
