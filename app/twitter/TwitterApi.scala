package twatcher.twitter

import twatcher.twitter.TwitterUris._
import twatcher.twitter.TwitterReads._

import play.api.libs.json.{JsValue, Reads}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.{WS, WSRequest}
import play.api.Play.current

import scala.concurrent.{Await, ExecutionContext, Future}
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
    def get(url: String, token: RequestToken, param: (String, String)*)
      (implicit ec: ExecutionContext): Future[JsValue] = {
        val request = buildRequest(url, token, param: _*)
        val fullUrl = getFullUrl(request)
        TwitterCache.getAs[JsValue](fullUrl, token).fold(
          for {
            response  <- request.get()
            validated <- errorCheck(response.json)
            _         =  TwitterCache.set(fullUrl, token, validated, 15.minutes)
          } yield validated
        )(Future.successful(_))
      }

    def get[T : Reads](url: String, token: RequestToken, param: (String, String)*)(implicit ec: ExecutionContext): Future[T] =
      get(url, token, param: _*).map(_.as[T])

    /**
     * Send POST request to Twitter
     */
    def post(url: String, token: RequestToken, param: (String, String)*)(implicit ec: ExecutionContext): Future[JsValue] = {
      val request = buildRequest(url, token, param: _*)
      for {
        response  <- request.post("") // have to POST nothing
        validated <- errorCheck(response.json)
      } yield validated
    }

    def post[T: Reads](url: String, token: RequestToken, param: (String, String)*)(implicit ec: ExecutionContext): Future[T] =
      post(url, token, param: _*).map(_.as[T])

    /**
     * Solve "cursor"
     *   if next cursor exists, Some(cursorValue)
     *   else (case 0) None
     */
    def solveCursor[T : Reads](json: JsValue, key: String): (T, Option[Long]) = {
      val contents = (json \ key).as[T]
      val nextCursor = (json \ "next_cursor").as[Long]

      if(nextCursor == 0) {
        (contents, None)
      } else {
        (contents, Some(nextCursor))
      }
    }

    /**
     * Load all available data by cursor
     * @param f: Function which loads result list and next cursor by current cursor
     * @param cursor: current cursor
     * @param result: already obtained result
     */
    private[this] def getAllByCursor[T](
      f: Long => Future[(List[T], Option[Long])]
    , cursor: Long
    , result: Future[List[T]]
    )(implicit ec: ExecutionContext): Future[List[T]] = {
      // load data
      f(cursor).flatMap { case (resultList, nextCursorOp) =>
        // join current and obtained list
        val newResult = result.map(_ ++ resultList)
        // if next cursor exists, call this function again
        nextCursorOp.fold(newResult)(getAllByCursor(f, _, newResult))
      } recover {
        // if failed, get already obtained result
        case _ => Await.result(result, Duration.Inf)
      }
    }

    def getAllByCursor[T](f: Long => Future[(List[T], Option[Long])])
      (implicit ec: ExecutionContext): Future[List[T]] =
        getAllByCursor(f, -1L, Future(Nil))

    /**
     * Load all available data by max_id
     * @param f: Function which loads result list by max_id
     * @param maxId: current max_id
     * @param count: number to load
     * @param result: already obtained result
     */
    private[this] def getAllByMaxId[T](
      f: Long => Future[List[T]]
    , toMaxId: T => Long
    , maxId: Long
    , count: Int
    , result: Future[List[T]]
    )(implicit ec: ExecutionContext): Future[List[T]] = {
      // load data
      f(maxId).flatMap { resultList =>
        // join current and obtained list
        val newResult = result.map(_ ++ resultList)
        // if loaded nothing, no data remains
        if(resultList.length == 0) {
          newResult
        } else {
          // obtain new maxId: 1 smaller than the oldest tweet
          val newMaxId = resultList.map(toMaxId).min - 1L
          getAllByMaxId(f, toMaxId, newMaxId, count, newResult)
        }
      } recover {
        case _ => Await.result(result, Duration.Inf)
      }
    }
    /**
     * entrance API of getAllByMaxId
     * @param f0: first called function: no need of maxId
     * @param f1: called on 2nd or later
     */
    def getAllByMaxId[T](f0: => Future[List[T]], f1: Long => Future[List[T]], toMaxId: T => Long, count: Int)
      (implicit ec: ExecutionContext): Future[List[T]] = {
        f0.flatMap { resultList =>
          if(resultList.length == 0) {
            Future.successful(resultList)
          } else {
            val maxId = resultList.map(toMaxId).min - 1L
            getAllByMaxId(f1, toMaxId, maxId, count, Future.successful(resultList))
          }
        } recover {
          case _ => Nil
        }
      }
  }
}

sealed trait TwitterApiService { self: TwitterApiRepositoryComponent =>
  type TweetsResult = Future[List[Tweet]]

  /**
   * GET user timeline
   */
  def getTimeline(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult =
    twitterApiRepository.get[List[Tweet]](USER_TIMELINE, token, "user_id" -> userId.toString)

  def getTimeline(screenName: String, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult =
    twitterApiRepository.get[List[Tweet]](USER_TIMELINE, token, "screen_name" -> screenName)

  /**
   * GET user profile
   */
  def getProfile(token: RequestToken)(implicit ec: ExecutionContext): Future[User] = {
    twitterApiRepository.get[User](SELF_PROFILE, token)
  }

  /**
   * GET favorited tweets
   */
  private[this] def getFavorite(token: RequestToken, params: (String, String)*)(implicit ec: ExecutionContext): TweetsResult =
    twitterApiRepository.get[List[Tweet]](FAVORITES_LIST, token, params: _*)
  // by userId
  def getFavorite(userId: Long, count: Int, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult =
    getFavorite(token, "user_id" -> userId.toString, "count" -> count.toString)
  def getFavorite(userId: Long, maxId: Long, count: Int, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult =
    getFavorite(token, "user_id" -> userId.toString, "max_id" -> maxId.toString, "count" -> count.toString)
  // by screenName
  def getFavorite(screenName: String, count: Int, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult =
    getFavorite(token, "screen_name" -> screenName, "count" -> count.toString)
  def getFavorite(screenName: String, maxId: Long, count: Int, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult =
    getFavorite(token, "screen_name" -> screenName, "max_id" -> maxId.toString, "count" -> count.toString)

  /**
   * GET all favorites
   */
  def getAllFavorite(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult = {
    val count = 200 // max value of count
    twitterApiRepository.getAllByMaxId[Tweet](
      f0 = getFavorite(userId, count, token)
    , f1 = (maxId: Long) => getFavorite(userId, maxId, count, token)
    , toMaxId = (t: Tweet) => t.id
    , count
    )
  }
  def getAllFavorite(screenName: String, token: RequestToken)(implicit ec: ExecutionContext): TweetsResult = {
    val count = 200
    twitterApiRepository.getAllByMaxId[Tweet](
      f0 = getFavorite(screenName, count, token)
    , f1 = (maxId: Long) => getFavorite(screenName, maxId, count, token)
    , toMaxId = (t: Tweet) => t.id
    , count
    )
  }

  type IdListResult = Future[(List[Long], Option[Long])]

  /**
   * GET following
   */
  private[this] def getFollowing(token: RequestToken, params: (String, String)*)(implicit ec: ExecutionContext): IdListResult = {
    twitterApiRepository.get(FRIENDS_IDS, token, params: _*).map{ response =>
      twitterApiRepository.solveCursor[List[Long]](response, "ids")
    }
  }

  // by userId
  def getFollowing(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowing(token, "user_id" -> userId.toString)
  def getFollowing(userId: Long, cursor: Long, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowing(token, "user_id" -> userId.toString, "cursor" -> cursor.toString)
  // by screenName
  def getFollowing(screenName: String, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowing(token, "screen_name" -> screenName)
  def getFollowing(screenName: String, cursor: Long, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowing(token, "screen_name" -> screenName, "cursor" -> cursor.toString)

  def getAllFollowing(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[List[Long]] =
    twitterApiRepository.getAllByCursor(
      f = (cursor: Long) => getFollowing(userId, cursor, token)
    )
  def getAllFollowing(screenName: String, token: RequestToken)(implicit ec: ExecutionContext): Future[List[Long]] =
    twitterApiRepository.getAllByCursor(
      f = (cursor: Long) => getFollowing(screenName, cursor, token)
    )

  /**
   * GET followers
   */
  private[this] def getFollowers(token: RequestToken, params: (String, String)*)(implicit ec: ExecutionContext): IdListResult = {
    twitterApiRepository.get(FOLLOWERS_IDS, token, params: _*).map{ response =>
      twitterApiRepository.solveCursor[List[Long]](response, "ids")
    }
  }

  // by userId
  def getFollowers(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowers(token, "user_id" -> userId.toString)
  def getFollowers(userId: Long, cursor: Long, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowers(token, "user_id" -> userId.toString, "cursor" -> cursor.toString)
  // by screenName
  def getFollowers(screenName: String, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowers(token, "screen_name" -> screenName)
  def getFollowers(screenName: String, cursor: Long, token: RequestToken)(implicit ec: ExecutionContext): IdListResult =
    getFollowers(token, "screen_name" -> screenName, "cursor" -> cursor.toString)

  def getAllFollowers(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[List[Long]] =
    twitterApiRepository.getAllByCursor(
      f = (cursor: Long) => getFollowers(userId, cursor, token)
    )
  def getAllFollowers(screenName: String, token: RequestToken)(implicit ec: ExecutionContext): Future[List[Long]] =
    twitterApiRepository.getAllByCursor(
      f = (cursor: Long) => getFollowing(screenName, cursor, token)
    )

  /**
   * POST Tweet to Twitter
   */
  def tweet(status: String, token: RequestToken)(implicit ec: ExecutionContext): Future[Tweet] =
    twitterApiRepository.post[Tweet](STATUSES_UPDATE, token, "status" -> status)

  /**
   * POST delete tweet
   */
  def delete(tweetId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[Tweet] =
    twitterApiRepository.post[Tweet](STATUSES_DESTROY(tweetId), token)

  /**
   * POST update profile
   */
  private[this] def updateProfile(token: RequestToken, params: (String, String)*)(implicit ec: ExecutionContext): Future[User] =
    twitterApiRepository.post[User](PROFILE_UPDATE, token, params: _*)
  // update name
  def updateName(name: String, token: RequestToken)(implicit ec: ExecutionContext): Future[User] =
    if(name.length <= 20) {
      updateProfile(token, "name" -> name)
    } else {
      Future.failed(new Exception("Name length maximum is 20"))
    }
  // update description
  def updateDescription(description: String, token: RequestToken)(implicit ec: ExecutionContext): Future[User] =
    if(description.length <= 160) {
      updateProfile(token, "description" -> description)
    } else {
      Future.failed(new Exception("Description length maximum is 160"))
    }

  /**
   * POST delete favorite
   */
  def unfavorite(tweetId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[Tweet] =
    twitterApiRepository.post[Tweet](FAVORITES_DESTROY, token, "id" -> tweetId.toString)

  /**
   * POST block user
   */
  def blockUser(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[User] =
    twitterApiRepository.post[User](BLOCKS_CREATE, token, "user_id" -> userId.toString)

  /**
   * POST unblock user
   */
  def unblockUser(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[User] =
    twitterApiRepository.post[User](BLOCKS_DESTROY, token, "user_id" -> userId.toString)

  /**
   * POST block -> unblock user
   */
  def goodbye(userId: Long, token: RequestToken)(implicit ec: ExecutionContext): Future[User] =
    blockUser(userId, token).flatMap(_ => unblockUser(userId, token))
}

abstract class TwitterApi(ck: ConsumerKey) extends TwitterApiRepositoryComponent with TwitterApiService {
  protected[this] val consumerKey = ck
  val twitterApiRepository = new TwitterApiRepository {
    override protected[this] val ck = consumerKey
  }
}
