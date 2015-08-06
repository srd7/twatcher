package twatcher.controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class AppController extends Controller {
  def index(str: String) = Action {
    Ok
  }
}
