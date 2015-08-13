package twatcher.actors

import twatcher.models.Script

import play.api.Logger

import akka.actor.{Actor, PoisonPill}

import scala.sys.process._

class ScriptActor extends Actor {
  def receive = {
    case ScriptMessage(script) => executeScript(script)
    case Exit()                => // Do nothing: Script Actor will suicide when script finish
    case _                     => // Do nothing
  }

  private[this] def executeScript(script: Script) = {
    if (isWindows) {
      s"cmd /c ${script.path}".!
    } else {
      s"./${script.path}"
    }

    Logger.info("Script Actor finish")
    context.parent ! Exit()
    self ! PoisonPill
  }

  private[this] def isWindows: Boolean =
    System.getProperty("os.name") contains "Windows"

}
