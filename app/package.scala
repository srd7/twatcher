import twatcher.logics.TwitterLogic
import twatcher.globals.{db, twitter, tokenList, scriptList}

import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.sys.process._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
  }

  override def onStop(app: Application) {
    db.shutdown
  }

}
