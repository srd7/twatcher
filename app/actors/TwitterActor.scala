package twatcher.actors

import twatcher.globals.twitter
import twatcher.logics.TwitterLogic
import twatcher.models.Account

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

import akka.actor._

class TwitterActor extends Actor {
  def receive = {
    case AccountMessage(account) => executeTwitterAction(account)
    case Exit()                  => finish()
    case _                       => // Do nothing
  }

  private[this] def executeTwitterAction(account: Account) = {
    // Execute action asynchronously
    if(account.goodbyeFlag) {
      Logger.info(s"@${account.screenName} says goodbye")
      createExecuter() ! Goodbye(account)
    }
    if(account.tweetDeleteFlag) {
      Logger.info(s"@${account.screenName} deletes tweets")
      createExecuter() ! TweetDelete(account)
    }
    if(account.favoriteDeleteFlag) {
      Logger.info(s"@${account.screenName} deletes favorite")
      createExecuter() ! FavoriteDelete(account)
    }
    if(account.updateProfile.isDefined) {
      Logger.info(s"@${account.screenName} changes profile")
      createExecuter() ! UpdateProfile(account)
    }

    // Finish executing if no action registered
    self ! Exit()
  }

  /**
   * Create child TwitterExecuteActor
   */
  private[this] def createExecuter(): ActorRef = {
    Logger.info("Twitter Execute Actor created.")
    context.actorOf(Props(classOf[TwitterExecuteActor]))
  }


  /**
   * Count children and if no children: finish all executing,
   *   tell parent BatchActor to have finished and kill itself
   */
  private[this] def finish() {
    if (context.children.size == 0) {
      Logger.info("Twitter Actor finish")
      context.parent ! Exit()
      self ! PoisonPill
    } else {
      Logger.info(s"Twitter Actor has ${context.children.size} task(s).")
    }
  }
}

class TwitterExecuteActor extends Actor {

  def receive = {
    case Goodbye(account)        => execute(TwitterLogic.goodbye(twitter, account))
    case TweetDelete(account)    => execute(TwitterLogic.deleteTweets(twitter, account))
    case FavoriteDelete(account) => execute(TwitterLogic.unfavorite(twitter, account))
    case UpdateProfile(account)  => execute(TwitterLogic.updateTwitterProfile(twitter, account))
    case Exit()                  => // Do nothing: Twitter Execute Actor takes suicide if action finish
    case _                       => // Do nothing
  }

  private[this] def execute(f: Future[Unit]) = {
    f onSuccess {
      case _ => finish()
    }
    f onFailure {
      case e: Throwable =>
        Logger.error("TwitterExecuteActor gets error", e)
        finish()
    }
  }

  /**
   * Tell parent TwitterActor to have finished executing and kill itself
   */
  private[this] def finish() {
    Logger.info("Twitter Execute Actor finish")
    context.parent ! Exit()
    self ! PoisonPill
  }
}
