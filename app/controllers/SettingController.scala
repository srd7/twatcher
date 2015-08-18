package twatcher.controllers

import twatcher.controllers.forms.SettingForm
import twatcher.globals.{db, twitter}
import twatcher.models.{Accounts, Configs, Scripts}
import twatcher.logics.{FileLogic, TwitterLogic}

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

import scala.concurrent.Future

// class SettingController extends Controller {
object SettingController extends Controller {

  def updatePeriod = Action.async { implicit request =>
    SettingForm.periodForm.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest)
    , period => {
        db.run(Configs.update(period)).map{ _ =>
          Redirect(routes.AppController.showSetting)
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
          Redirect(routes.AppController.showSetting)
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
          Redirect(routes.AppController.showSetting)
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
          Redirect(routes.AppController.showSetting)
        } recover {
          case e: Exception => InternalServerError("db error")
        }
      }
    )
  }

  def updateAccount = Action.async { implicit request =>
    SettingForm.accountDetailForm.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest)
    , detailOnlyAccount => {
        db.run(Accounts.updateDetail(detailOnlyAccount)).map { _ =>
          Redirect(routes.AppController.showSetting)
        } recover {
          case e: Exception => InternalServerError("db error")
        }
      }
    )
  }

  def deleteAccount = Action.async { implicit request =>
    SettingForm.accountForm.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest)
    , userId => {
        db.run(Accounts.delete(userId)).map { _ =>
          Redirect(routes.AppController.showSetting)
        } recover {
          case e: Exception => InternalServerError("db error")
        }
      }
    )
  }

  /**
   * Check Twitter Authentication and update Twitter info
   */
  def checkAccount(userId: Long) = Action.async { implicit request =>
    db.run(Accounts.findByUserId(userId)).flatMap { accountOp =>
      accountOp.fold[Future[Result]](Future.successful(BadRequest))(account =>
        TwitterLogic.upsertUserProfile(twitter, account.token).map { newAccount =>
          Ok(Json.obj(
            "userId" -> newAccount.userId
          , "screenName" -> newAccount.screenName
          , "imageUrl" -> newAccount.imageUrl
          ))
        } recover {
          case e: Exception => BadRequest(Json.obj(
            "error" -> "authentication failed"
          , "userId" -> userId
          ))
        }
      )
    }
  }

  /**
   * Upload zip file, parse it, and insert tweet ids into DB
   */
  def insertTweetZip = Action.async(parse.multipartFormData) { implicit request =>
    request.body.file("zip").fold[Future[Result]](Future.successful(BadRequest)) { zip =>
      FileLogic.insertTweetZip(zip.ref.file).map { _ =>
        Redirect(routes.AppController.showSetting)
      } recover {
        case e: Exception => InternalServerError("db error")
      }
    }
  }
}
