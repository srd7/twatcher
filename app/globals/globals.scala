package twatcher

import twatcher.models.Json._
import twatcher.twitter.Twitter

import play.api.{Application, Configuration, Play}
import play.api.libs.json.Json

import scala.io.Source

object globals extends Config {
  private[this] val jsonConfig =
    Json.parse(jsonConfigFile).as[JsonConfigItem]

    // val jsonRaw = try {
    //   Json.parse(jsonConfigFile).as[JsonConfigItem]
    // } catch {
    //   case e: Throwable => {
    //     println("=" * 32)
    //     e.printStackTrace()
    //     println("=" * 32)
    //   }
    // }
  /**
   * Account token list
   */
  val tokenList = jsonConfig.tokenList
  // val tokenList = Nil
  /**
   * Period to detect death
   */
  val periodDay = jsonConfig.periodDay
  // val periodDay = 7

  /**
   * Static Twitter App
   */
  val twitter = new Twitter(jsonConfig.consumerKey.key, jsonConfig.consumerKey.secret)
  // val twitter = null

  /**
   * Scripts run when Twitter is not active
   */
  val scriptList = jsonConfig.scriptList
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

  private[this] def getString(path: String) = getPlayConfig(_.getString (path))

  private[this] def getPlayConfig[T](f: Configuration => Option[T]) =
    Play.maybeApplication.flatMap(p => f(p.configuration)).getOrElse {
      throw new RuntimeException("config key not found")
    }
}
