package twatcher.controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object AppController extends Controller {
  def index = Action {
    Ok(twatcher.globals.tokenList.toString)
  }

  def isActive = Action.async {
    twatcher.logics.TwitterLogic.isActiveAll(twatcher.globals.twitter, twatcher.globals.tokenList).map { flag =>
      if (flag) Ok("active") else Ok("not active")
    } recover {
      case e: Exception => BadRequest(e.getMessage)
    }
  }
}
