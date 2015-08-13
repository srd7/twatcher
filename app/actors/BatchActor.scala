package twatcher.actors

import twatcher.models.{Account, Script}
import twatcher.logics.BatchLogic

import play.api.Logger

import akka.actor._

/**
 * Actor of assign and manage
 *   1. Executing scripts
 *   2. Twitter API Action
 */
class BatchActor extends Actor {
  def receive = {
    case ScriptList(scriptList)   => assignScripts(scriptList)
    case AccountList(accountList) => assignAccounts(accountList)
    case IsRunning()              => sender() ! RunningStatus(isRunning)
    case Exit()                   => exit()
  }

  /**
   * Assign Scripts, one script to one ScriptActor
   */
  private[this] def assignScripts(scriptList: List[Script]) = {
    scriptList.foreach { script =>
      // Create child actor.
      Logger.info("Script Actor created")
      val scriptActor = context.actorOf(Props(classOf[ScriptActor]))
      scriptActor ! ScriptMessage(script)
    }
  }

  /**
   * Assing Twitter accounts, one account to one TwitterActor
   */
  private[this] def assignAccounts(accountList: List[Account]) = {
    accountList.foreach { account =>
      // Create child actor
      Logger.info("Twitter Actor created")
      val twitterActor = context.actorOf(Props(classOf[TwitterActor]))
      twitterActor ! AccountMessage(account)
    }
  }

  private[this] def isRunning: Boolean = {
    context.children.size > 0
  }

  /**
   * If there is no children, execute App exit script
   */
  private[this] def exit() = {
    if (isRunning) {
      BatchLogic.exit()
      self ! PoisonPill
    } else {
      Logger.info(s"Batch Actor has ${context.children.size} task(s).")
      // Check children actors
      context.children.foreach(_ ! Exit())
    }
  }
}
