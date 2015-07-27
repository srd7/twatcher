package twatcher.logics

import twatcher.globals.periodDay
import twatcher.models.RequestToken
import twatcher.twitter.Twitter

import scala.concurrent.{ExecutionContext, Future}

object TwitterLogic extends FutureUtils {
  /**
   * Examine that an account is updated or not
   * @param twitter Twitter App instance
   * @param token Request Token of the target account
   */
  private[this] def isActive(twitter: Twitter, token: RequestToken)
    (implicit ec: ExecutionContext): Future[Boolean] = {
      // mapping response
      // Future[List[Tweet]]
      //   => Future[Option[Tweet]]
      //   => Future[Option[Date]]
      //   => Future[Boolean]
      twitter.getTimeline(token.screenName, token.toOAuthToken).map { tweetList =>
        // The latest tweet must be head
        val latestDateOp = tweetList.headOption.map(_.createdAt)
        // If latestDateOp is None, the judge is true
        latestDateOp.fold(false){ date =>
          // The latest is newer than (Now - period) ?
          date.getTime > System.currentTimeMillis - periodDay.toLong * 86400000L
        }
      }
    }

  /**
   * Examine all account
   */
  def isActiveAll(twitter: Twitter, tokenList: List[RequestToken]): Future[Boolean] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    // Require to be active at least one account,
    swapListFut(tokenList.map(isActive(twitter, _))).map(_.exists(identity))
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