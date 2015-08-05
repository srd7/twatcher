package twatcher.controllers

import twatcher.controllers.forms.SettingForm
import twatcher.globals.db
import twatcher.models.{Accounts, Configs, Scripts}

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object SettingController extends Controller {

  def showSetting = Action.async { implicit request =>
    for {
      accountList <- db.run(Accounts.get).map(_.toList)
      period      <- db.run(Configs.get).map(_.period)
    } yield {
      Ok(views.html.showSetting(period, accountList))
    }
  }

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

  def createScript = Action.async { implicit request =>
    SettingForm.scriptForm.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest)
    , script => {
        db.run(Scripts.insert(script)).map { _ =>
          Ok
        } recover {
          case e: Exception => InternalServerError("db error")
        }
      }
    )
  }

  def updateScript = Action.async { implicit request =>
    SettingForm.scriptForm.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest)
    , script => {
        db.run(Scripts.update(script)).map { _ =>
          Ok
        } recover {
          case e: Exception => InternalServerError("db error")
        }
      }
    )
  }

  def deleteScript = Action.async { implicit request =>
    SettingForm.scriptForm.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest)
    , script => {
        db.run(Scripts.delete(script)).map { _ =>
          Ok
        } recover {
          case e: Exception => InternalServerError("db error")
        }
      }
    )
  }
}
