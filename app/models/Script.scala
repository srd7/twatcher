package twatcher.models

import slick.driver.H2Driver.api._

case class Script(
  path: String
)

class Scripts(tag: Tag) extends Table[Script](tag, "SCRIPT") {
  def path = column[String]("PATH")
  def * = (path) <> (Script.apply _, Script.unapply)
}

object Scripts extends TableQuery(new Scripts(_)) {
  def initial = this.result
}
