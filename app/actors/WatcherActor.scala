package twatcher.actors

import twatcher.logics.BatchLogic

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger

import scala.concurrent.Future

import akka.actor.Actor

class WatcherActor extends Actor {
  def receive = {
    case Watch() => watching()
  }

  private[this] def watching() = {
    Logger.info("Check Twitter...")
    BatchLogic.check()
  }
}
