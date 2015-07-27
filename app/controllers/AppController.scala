package twatcher.controllers

import play.api.mvc._

object AppController extends Controller {
  def index = Action {
    Ok(twatcher.globals.tokenList.toString)
  }
}
