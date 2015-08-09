package twatcher.logics

import twatcher.globals.{db, twitter}
import twatcher.models.{Accounts, Scripts}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.sys.process._

object BatchLogic {
  def run() {
    val runningFut = for {
      accountList <- db.run(Accounts.get)
      checkResult <- TwitterLogic.isActiveAll(twitter, accountList.toList)
    } yield checkResult

    runningFut.map { isActive =>
      if (isActive) {
        // Do not have to run script
        println("You are alive!")
      } else {
        // Execute script
        println("You are dead!")
        db.run(Scripts.get).map { scriptList =>
          scriptList.foreach { scriptName =>
            if (isWindows) {
              s"cmd /c $scriptName".!
            } else {
              s"./${scriptName}".!
            }
          }
        }
      }
    }

    runningFut onSuccess {
      case _ => exit()
    }

    runningFut onFailure {
      case e: Throwable => {
        e.printStackTrace()
        exit()
      }
    }
  }

  def exit() {
    (10 to 1 by -1).foreach { n =>
      println(s"exit program in $n seconds...")
      Thread.sleep(1000L)
    }
    if (isWindows) {
      "cmd /c exit.bat".!
    }
  }

  private[this] def isWindows: Boolean =
    System.getProperty("os.name") contains "Windows"
}
