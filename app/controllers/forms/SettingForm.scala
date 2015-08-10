package twatcher.controllers.forms

import twatcher.models.{DetailOnlyAccount, Script}

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

  /**
   * WORKAROUND
   * bind default HTML5 checkbox
   */
  private[this] val checkbox =
    optional(nonEmptyText).transform(_ == Some("on"), (flag: Boolean) => if(flag) Some("on") else None)

  val accountDetailForm = Form(
    mapping(
      "userId" -> longNumber
    , "goodbyeFlag" -> checkbox
    , "tweetDeleteFlag" -> checkbox
    , "favoriteDeleteFlag" -> checkbox
    , "updateProfile" -> optional(nonEmptyText)
    )(DetailOnlyAccount.apply)(DetailOnlyAccount.unapply _)
  )
}
