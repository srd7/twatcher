import twatcher.logics.BatchLogic
import twatcher.globals.{db, mode}
import twatcher.mode._
import twatcher.actors._

import play.api._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._

import akka.actor.Props

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    mode match {
      case Batch   => BatchLogic.run()
      case Server  => startActor()
      case Setting => println("open http://localhost:9000 by browser.")
      case Default => println("default")
    }
  }

  override def onStop(app: Application) {
    db.shutdown
  }

  private[this] def startActor() {
    Akka.system.scheduler.schedule(
      initialDelay = 10.seconds
    , interval     = 1.hour
    , receiver     = Akka.system.actorOf(Props[WatcherActor])
    , message      = Watch()
    )
  }
}
