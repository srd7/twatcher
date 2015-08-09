package twatcher.controllers

import twatcher.models.{Accounts, Configs, Scripts}
import twatcher.globals.db

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.routing.JavaScriptReverseRouter
import play.api.i18n.{I18nSupport, MessagesApi}

import javax.inject.Inject

class AppController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def index(str: String) = Action {
    Ok(views.html.index())
  }

  def showSetting = Action.async { implicit request =>
    for {
      period      <- db.run(Configs.get).map(_.period)
      accountList <- db.run(Accounts.get).map(_.toList)
      scriptList  <- db.run(Scripts.get).map(_.toList)
    } yield {
      Ok(views.html.showSetting(period, accountList, scriptList))
    }
  }


  def jsRoutes = Action { implicit request =>
    Ok(JavaScriptReverseRouter("router")(
      routes.javascript.SettingController.checkAccount
    )).as("text/javascript")
  }

}
