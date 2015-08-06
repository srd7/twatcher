package twatcher.controllers.forms

import twatcher.models.Script

import play.api.data.Form
import play.api.data.Forms._

object SettingForm {
  val periodForm = Form(
    "period" -> number
  )

  val scriptForm = Form(
    mapping(
      "id"   -> default(number, -1)
    , "path" -> nonEmptyText
    )(Script.apply)(Script.unapply _)
  )

  val accountForm = Form(
    "userId" -> longNumber
  )
}
