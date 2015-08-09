import twatcher.logics.BatchLogic
import twatcher.globals.{db, mode}
import twatcher.mode._

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.sys.process._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    mode match {
      case Batch   => BatchLogic.run()
      case Server  => println("server") // TODO: Start Watching Actor
      case Setting => println("setting")
      case Default => println("default")
    }
  }

  override def onStop(app: Application) {
    db.shutdown
  }

}
