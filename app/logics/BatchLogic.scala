package twatcher.logics

import twatcher.globals.{db, twitter}
import twatcher.models._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger

import scala.concurrent.Future
import scala.sys.process._

object BatchLogic {
  def check(): Future[Unit] = {
    for {
      periodDay   <- db.run(Configs.get).map(_.period)
      accountList <- db.run(Accounts.get).map(_.toList)
    } yield {
      isActiveFut(periodDay, accountList).map { isActive =>
        if (isActive) {
          // Do not have to run script
          Logger.info("You are alive!")
        } else {
          // Execute script
          Logger.info("You are dead!")
          db.run(Scripts.get).foreach { scripts =>
            executeScript(scripts.toList)
            executeTwitterAction(accountList)
          }
        }
      }
    }
  }

  /**
   * Check accounts and exit program
   */
  def batch() = {
    val runningFut = check()
    runningFut onSuccess {
      case _ => waitForExit()
    }

    runningFut onFailure {
      case e: Throwable => {
        e.printStackTrace()
        waitForExit()
      }
    }
  }

  /**
   * Check whecher accounts are active or not
   */
  private[this] def isActiveFut(periodDay: Int, accountList: List[Account]): Future[Boolean] =
    TwitterLogic.isActiveAll(twitter, periodDay, accountList.toList)

  /**
   * Execute scripts asynchronously
   */
  private[this] def executeScript(scriptList: List[Script]): Unit = {
    scriptList.foreach { script =>
      if (isWindows) {
        s"cmd /c ${script.path}".!
      } else {
        s"./${script.path}".!
      }
    }
  }

  /**
   * Execute actions to Twitter
   */
  private[this] def executeTwitterAction(accountList: List[Account]): Unit = {
    accountList.foreach( account => Future {
      // Execute action asynchronously
      // Comming soon...
      // TwitterLogic.goodbye(twitter, account)
      // TwitterLogic.unfavorite(twitter, account)
    })
  }

  private[this] def waitForExit() {
    (10 to 1 by -1).foreach { n =>
      Logger.info(s"exit program in $n seconds...")
      Thread.sleep(1000L)
    }
    exit()
  }

  def exit() {
    if (isWindows) {
      "cmd /c exit.bat".!
    }
  }

  private[this] def isWindows: Boolean =
    System.getProperty("os.name") contains "Windows"
}
