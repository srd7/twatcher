package twatcher.actors

import twatcher.logics.TwitterLogic
import twatcher.globals.{db, twitter}
import twatcher.models.Accounts

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger

import akka.actor.Actor

class WatcherActor extends Actor {
  def receive = {
    case Watch() => watching()
  }

  private[this] def watching() = {
    Logger.info("Check Twitter...")
    // TODO: fix workaround
    val runningFut = (for {
      accountList <- db.run(Accounts.get)
      checkResult <- TwitterLogic.isActiveAll(twitter, accountList.toList)
    } yield (checkResult, accountList.toList)).map { case (isActive, accountList) =>
      if (isActive) {
        Logger.info("You are alive!")
      } else {
        Logger.info("You are dead!")
        TwitterLogic.goodbye(twitter, accountList)
      }
    }
  }
}
