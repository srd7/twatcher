package twatcher

import twatcher.models.Json._

import play.api.{Application, Configuration, Play}
import play.api.libs.json.Json

import scala.io.Source

object globals extends Config {
  private[this] val jsonConfig =
    Json.parse(jsonConfigFile).as[JsonConfigItem]

  /**
   * Account token list
   */
  val tokenList = jsonConfig.tokenList
  /**
   * Period to detect death
   */
  val periodDay = jsonConfig.periodDay
}

sealed trait Config {

  /**
   * Try to read config json file(default: config.json)
   */
  protected[this] val jsonConfigFile: java.io.InputStream = {
    val filename = getString("twatcher.json.config.file")

    Play.maybeApplication.flatMap(p => p.resourceAsStream(filename)) match {
      case Some(stream) => stream
      case None => throw new RuntimeException(s"file $filename not found.")
    }
  }

  private[this] def getString(path: String) = getPlayConfig(_.getString (path)).get

  private[this] def getPlayConfig[T](f: Configuration => Option[T]) =
    Play.maybeApplication.flatMap(p => f(p.configuration))
}
