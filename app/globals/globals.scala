package twatcher

import twatcher.twitter.Twitter
import twatcher.models.{Accounts, Configs, Scripts}

import play.api.{Application, Configuration, Play}
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Json

import slick.jdbc.JdbcBackend.Database
import slick.dbio.{DBIOAction, NoStream}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object globals extends Config {

  // Account token list
  val tokenList = fromDB(Accounts.initial)

  // Period to detect death
  // val periodDay = jsonConfig.periodDay
  private[this] val config = fromDB(Configs.initial)
  val periodDay = config.period

  // Static Twitter App
  val twitter = new Twitter(twitterConsumerKey, twitterConsumerSecret)

  // Scripts run when Twitter is not active
  val scriptList = fromDB(Scripts.initial).map(_.path)

  // database
  def db = Database.forDataSource(DB.getDataSource())

  private[this] def fromDB[T](a: DBIOAction[T, NoStream, Nothing]): T =
    Await.result(db.run(a), Duration.Inf)
}

sealed trait Config {
  protected[this] val twitterConsumerKey    = getString("twatcher.twitter.consumer.key")
  protected[this] val twitterConsumerSecret = getString("twatcher.twitter.consumer.secret")

  private[this] def getString(path: String) = getPlayConfig(_.getString (path))

  private[this] def getPlayConfig[T](f: Configuration => Option[T]): T =
    Play.maybeApplication.flatMap(p => f(p.configuration)).getOrElse {
      throw new RuntimeException("config key not found")
    }
}
