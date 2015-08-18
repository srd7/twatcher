package twatcher.logics

import twatcher.globals.{db, twitter}
import twatcher.models._
import twatcher.actors._

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.sys.process._

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

object BatchLogic {
  private[this] val batchActor = Akka.system.actorOf(Props[BatchActor])
  private[this] implicit val timeout = Timeout(5.seconds)
  def check(): Future[Boolean] = {
    val resultFut = for {
      periodDay   <- db.run(Configs.get).map(_.period)
      accountList <- db.run(Accounts.get).map(_.toList)
      _           <- TwitterLogic.insertTweetsAll(twitter, accountList)
      result      <- isActiveFut(periodDay, accountList)
      runningSt   <- batchActor ? IsRunning()
    } yield (result, runningSt, accountList)


    resultFut onSuccess {
      case (true, _, _)  => {
        // Do not have to run script
        Logger.info("You are alive!")
      }
      case (false, RunningStatus(false), accountList) => {
        // Execute script
        Logger.info("You are dead!")
        db.run(Scripts.get).foreach { scripts =>
          batchActor ! ScriptList(scripts.toList)
          batchActor ! AccountList(accountList)
        }
      }
      case _ => {
        Logger.info("batching has been already doing.")
      }
    }

    resultFut.map(_._1)
  }

  /**
   * Check accounts and exit program
   */
  def batch() = {
    val resultFut = check()
    resultFut onSuccess {
      case true  => exit()
      case false => // BatchActor kills App so here have nothing to do.
    }

    resultFut onFailure {
      case e: Throwable => {
        e.printStackTrace()
        exit()
      }
    }
  }

  /**
   * Check whecher accounts are active or not
   */
  private[this] def isActiveFut(periodDay: Int, accountList: List[Account]): Future[Boolean] =
    TwitterLogic.isActiveAll(twitter, periodDay, accountList)

  def exit() {
    (10 to 1 by -1).foreach { n =>
      Logger.info(s"exit program in $n seconds...")
      Thread.sleep(1000L)
    }
    if (isWindows) {
      "cmd /c exit.bat".!
    } else {
      "./exit".!
    }
  }

  private[this] def isWindows: Boolean =
    System.getProperty("os.name") contains "Windows"

  /**
   * Listen Batch Actor whether app can exit.
   * Execute exit script if app can.
   */
  private[this] def listenToExit() = {
    Akka.system.scheduler.schedule(
      initialDelay = 10.seconds
    , interval     = 10.seconds
    , receiver     = batchActor
    , message      = Exit()
    )
  }
}
