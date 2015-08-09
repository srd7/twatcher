package twatcher.controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.routing.JavaScriptReverseRouter

class AppController extends Controller {
  def index(str: String) = Action {
    Ok
  }

  def jsRoutes = Action { implicit request =>
    Ok(JavaScriptReverseRouter("router")(
      routes.javascript.SettingController.checkAccount
    )).as("text/javascript")
  }

}
