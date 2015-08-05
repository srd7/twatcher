package twatcher.controllers

import twatcher.controllers.forms.SettingForm
import twatcher.globals.db
import twatcher.models.Configs

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object SettingController extends Controller {
  def updatePeriod = Action.async { implicit request =>
    SettingForm.periodForm.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest)
    , period => {
        db.run(Configs.update(period)).map{ _ =>
          Ok
        } recover {
          case e: Exception => InternalServerError("db error")
        }
      }
    )
  }
}
