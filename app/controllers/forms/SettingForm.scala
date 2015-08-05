package twatcher.controllers.forms

import play.api.data.Form
import play.api.data.Forms._

object SettingForm {
  val periodForm = Form(
    "period" -> number
  )
}
