package twatcher.models

import slick.driver.H2Driver.api._

case class Config(
  period: Int
)

class Configs(tag: Tag) extends Table[Config](tag, "CONFIG") {
  def period = column[Int]("PERIOD")
  def * = (period) <> (Config.apply _, Config.unapply)
}

object Configs extends TableQuery(new Configs(_)) {
  def get = this.result.head
  def update(period: Int) = this.map(_.period).update(period)
}
