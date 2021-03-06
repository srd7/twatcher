package twatcher.logics

import twatcher.globals.db
import twatcher.models.{Account, Accounts, Configs, Tweet => DBTweet, Tweets}
import twatcher.twitter.{Twitter, User}

import play.api.libs.oauth.RequestToken
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

import scalaz.Scalaz._

object TwitterLogic {
  /**
   * Examine that an account is updated or not
   * @param twitter Twitter App instance
   * @param account
   */
  private[this] def isActive(twitter: Twitter, periodDay: Int, account: Account)
    (implicit ec: ExecutionContext): Future[Boolean] = {
      val count = 10 // just in case
      // mapping response
      // Future[List[Tweet]]
      //   => Future[Option[Tweet]]
      //   => Future[Option[Date]]
      //   => Future[Boolean]
      twitter.getTimeline(account.screenName, count, account.token).map { tweetList =>
        // The latest tweet must be head
        // If latestDateOp is None, the judge is true
        val result = tweetList.headOption.map(_.createdAt).fold(false) { date =>
          // The latest is newer than (Now - period) ?
          date.getTime > System.currentTimeMillis - periodDay.toLong * 86400000L
        }
        val resultStr = if (result) "active" else "not active"
        Logger.info(s"[TwitterLogic] @${account.screenName} is ${resultStr}")
        result
      }
    }

  /**
   * Examine all account
   */
  def isActiveAll(twitter: Twitter, periodDay: Int, accountList: List[Account]): Future[Boolean] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    // Require to be active at least one account,
    accountList.traverseU(isActive(twitter, periodDay, _)).map(isActiveList =>
      isActiveList.isEmpty || isActiveList.exists(identity)
    )
  }

  /**
   * Get profile and Insert into DB about user
   */
  def upsertUserProfile(twitter: Twitter, token: RequestToken): Future[Account] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    for {
      profile <- twitter.getProfile(token)
      accountOp <- db.run(Accounts.findByUserId(profile.twitterUserId))
      account = accountOp.fold(Account(
          userId            = profile.twitterUserId
        , screenName        = profile.screenName
        , imageUrl          = profile.profileImageUrl
        , accessToken       = token.token
        , accessTokenSecret = token.secret
      ))(_.copy(
        screenName        = profile.screenName
      , imageUrl          = profile.profileImageUrl
      , accessToken       = token.token
      , accessTokenSecret = token.secret
      ))
      _ <- db.run(Accounts.upsert(account))
    } yield account
  }

  /**
   * Say goodbye to all following and followers
   */
  def goodbye(twitter: Twitter, account: Account): Future[Unit] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    val waitTime = 500L
    val screenName = account.screenName
    val tokenPair = account.token
    for {
      following <- twitter.getAllFollowing(screenName, tokenPair)
      followers <- twitter.getAllFollowers(screenName, tokenPair)
      joined = (following ++ followers).distinct
      _ = Logger.info(s"@${screenName} says goodbye to ${joined.length} accounts.")
      _ = joined.foreach { userId =>
        twitter.goodbye(userId, tokenPair)
        Thread.sleep(waitTime)
      }
    } yield ()
  }

  /**
   * Delete all tweets available
   */
  def deleteTweets(twitter: Twitter, account: Account): Future[Unit] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    val waitTime = 500L
    val tokenPair = account.token

    db.run(Tweets.get(account.userId)).map { tweetList =>
      tweetList.foreach { tweet =>
        twitter.delete(tweet.tweetId, tokenPair)
        Thread.sleep(waitTime)
      }
    }
  }

  /**
   * Delete all favorites
   */
  def unfavorite(twitter: Twitter, account: Account): Future[Unit] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    val waitTime = 500L
    val screenName = account.screenName
    val tokenPair = account.token

    twitter.getAllFavorite(screenName, tokenPair).map { tweetList =>
      tweetList.foreach { tweet =>
        twitter.unfavorite(tweet.id, tokenPair)
        Thread.sleep(waitTime)
      }
    }
  }

  /**
   * Update Twitter profile
   */
  def updateTwitterProfile(twitter: Twitter, account: Account): Future[Unit] = {
    account.updateProfile.fold(Future.successful(())) { profile =>
      import play.api.libs.concurrent.Execution.Implicits.defaultContext
      twitter.updateDescription(profile, account.token).map(_ => ())
    }
  }

  /**
   * Load tweets from Twitter and insert them to DB
   */
  private[this] def insertTweets(twitter: Twitter, account: Account)(implicit ec: ExecutionContext): Future[Unit] = {
    val userId = account.userId
    val screenName = account.screenName
    Logger.info(s"[TwitterLogic] @${screenName} start to insert tweets to DB")
    for {
      tweetList     <- twitter.getAllTweets(screenName, account.token)
      latestTweet   <- db.run(Tweets.latest(userId))
      latestTweetId =  latestTweet.fold(0L)(_.tweetId)
      dbTweetList   =  tweetList.withFilter(_.id > latestTweetId).map(apiTweet => DBTweet(userId, apiTweet.id))
      _             <- db.run(Tweets.insertAll(dbTweetList))
      _             =  Logger.info(s"[TwitterLogic] Inserted ${dbTweetList.length} tweet(s) to DB for @${screenName}")
    } yield ()
  }

  def insertTweetsAll(twitter: Twitter, accountList: List[Account]): Future[Unit] ={
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    accountList.traverseU_(insertTweets(twitter, _))
  }
}
