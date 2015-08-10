package twatcher.logics

import twatcher.globals.db
import twatcher.models.{Account, Accounts, Configs}
import twatcher.twitter.Twitter

import play.api.libs.oauth.RequestToken

import scala.concurrent.{ExecutionContext, Future}

object TwitterLogic extends FutureUtils {
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
        tweetList.headOption.map(_.createdAt).fold(false) { date =>
          // The latest is newer than (Now - period) ?
          date.getTime > System.currentTimeMillis - periodDay.toLong * 86400000L
        }
      }
    }

  /**
   * Examine all account
   */
  def isActiveAll(twitter: Twitter, periodDay: Int, accountList: List[Account]): Future[Boolean] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    // Require to be active at least one account,
    swapListFut(accountList.map(isActive(twitter, periodDay, _))).map(isActiveList =>
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
  def goodbye(twitter: Twitter, account: Account) = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    val waitTime = 500L
    val screenName = account.screenName
    val tokenPair = account.token
    for {
      following <- twitter.getAllFollowing(screenName, tokenPair)
      followers <- twitter.getAllFollowers(screenName, tokenPair)
      joined = (following ++ followers).distinct
      _ = println(s"@${screenName} says goodbye to ${joined.length} accounts.")
      _ = joined.foreach { userId =>
        twitter.goodbye(userId, tokenPair)
        Thread.sleep(waitTime)
      }
    } yield ()
  }

  /**
   * Delete all tweets available
   */
  def deleteTweets(twitter: Twitter, account: Account) = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    val waitTime = 500L
    val screenName = account.screenName
    val tokenPair = account.token

    twitter.getAllTweets(screenName, tokenPair).map { tweetList =>
      tweetList.foreach { tweet =>
        twitter.delete(tweet.id, tokenPair)
        Thread.sleep(waitTime)
      }
    }
  }

  /**
   * Delete all favorites
   */
  def unfavorite(twitter: Twitter, account: Account) = {
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
  def updateTwitterProfile(twitter: Twitter, account: Account) = {
    account.updateProfile.foreach { profile =>
      import play.api.libs.concurrent.Execution.Implicits.defaultContext
      twitter.updateDescription(profile, account.token)
    }
  }
}

trait FutureUtils {
  /**
   * Swap List[Future[T]] => Future[List[T]]
   */
  def swapListFut[T](base: List[Future[T]])(implicit ec: ExecutionContext): Future[List[T]] = {
    def loop(list: List[Future[T]], result: Future[List[T]]): Future[List[T]] = list match {
      case head :: tail => {
        val newResult = head.flatMap { t =>
          result.map(t :: _)
        }
        loop(tail, newResult)
      }
      case Nil => result.map(_.reverse)
    }
    loop(base, Future.successful(Nil))
  }
}
