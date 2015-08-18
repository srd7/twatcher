package twatcher.controllers

import twatcher.models.{Accounts, Configs, Scripts, Tweets}
import twatcher.globals.db
import twatcher.logics.BatchLogic

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
// import play.api.routing.JavaScriptReverseRouter
// import play.api.i18n.{I18nSupport, MessagesApi}

import scala.concurrent.Future

// import javax.inject.Inject

// Comment-outed imports can not be used at Play 2.3...

// class AppController @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {
object AppController extends Controller {
  def index(str: String) = Action {
    Ok(views.html.index())
  }

  def showSetting = Action.async { implicit request =>
    for {
      period      <- db.run(Configs.get).map(_.period)
      accountList <- db.run(Accounts.get).map(_.toList)
      counterMap  <- db.run(Tweets.count).map(_.toMap)
      scriptList  <- db.run(Scripts.get).map(_.toList)
    } yield {
      Ok(views.html.showSetting(period, accountList, counterMap, scriptList))
    }
  }

  def shutdown = Action {
    Future {
      BatchLogic.exit()
    }
    Ok(views.html.shutdown())
  }


  def jsRoutes = Action { implicit request =>
    // Ok(JavaScriptReverseRouter("router")(
    Ok(play.api.Routes.javascriptRouter("router")(
      routes.javascript.SettingController.checkAccount
    )).as("text/javascript")
  }

}
