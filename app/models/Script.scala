package twatcher.models

import slick.driver.H2Driver.api._

case class Script(
  id  : Int
, path: String
)

class Scripts(tag: Tag) extends Table[Script](tag, "SCRIPT") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def path = column[String]("PATH")
  def * = (id, path) <> (Script.tupled, Script.unapply)
}

object Scripts extends TableQuery(new Scripts(_)) {
  def initial = this.result
}
