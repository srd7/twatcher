import twatcher.logics.TwitterLogic
import twatcher.globals.{twitter, tokenList, scriptList}

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.sys.process._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    // If the mode is Prod i.e. disted binary
    //   check wheather dead or alive and run script.
    if(app.mode == Mode.Prod) {
      val runningFut = TwitterLogic.isActiveAll(twitter, tokenList).map { isActive =>
        if (isActive) {
          // Do not have to run script
          println("You are alive!")
        } else {
          // Run script
          println("You are dead!")
          if(isWindows) {
            scriptList.foreach { scriptName =>
              s"cmd /c $scriptName".!
            }
          } else {
            scriptList.foreach { scriptName =>
              s"./${scriptName}".!
            }
          }
        }
        ()
      }

      // Exit when batching is finished
      runningFut onSuccess {
        case _ => exit()
      }

      runningFut onFailure {
        case e: Throwable => {
          println(e.getMessage)
          exit()
        }
      }
    }
  }

  private[this] def exit() {
    (10 to 1 by -1).foreach { n =>
      println(s"exit program in $n seconds...")
      Thread.sleep(1000L)
    }

    // If Windows, run exit.bat
    if(isWindows) {
      "cmd /c exit.bat".!
    }
  }

  private[this] def isWindows: Boolean =
    System.getProperty("os.name") contains "Windows"

}
