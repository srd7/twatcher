package twatcher.controllers

import twatcher.globals.twitter
import twatcher.logics.TwitterLogic

import play.api.mvc._
import play.api.libs.oauth.RequestToken
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

class TwitterController extends Controller {
  final val SES_TOKEN = "token"
  final val SES_SECRET = "secret"

  /**
   * Redirect to Twitter Login
   */
  def login = Action { implicit request =>
    val redirectUrl = "http://" + request.host + routes.TwitterController.loginDone

    twitter.oauth.retrieveRequestToken(redirectUrl) match {
      case Right(t) => {
        Results.Redirect(twitter.oauth.redirectUrl(t.token)).withSession(
          SES_TOKEN  -> t.token
        , SES_SECRET -> t.secret
        )
      }
      case Left(e) => {
        InternalServerError(e.getMessage)
      }
    }
  }
  /**
   * Return from Twitter login
   */
  def loginDone = Action.async { implicit request =>
    // Check session and get parameter and extract info
    // If any info is lack, login is failed.
    val requestInfoOp = for {
      token  <- request.session.get(SES_TOKEN)
      secret <- request.session.get(SES_SECRET)
      verifier <- request.queryString.get("oauth_verifier").flatMap(_.headOption)
    } yield (RequestToken(token, secret), verifier)

    requestInfoOp.fold[Future[Result]](Future.successful(BadRequest)){ case (requestToken ,verifier) =>
      twitter.oauth.retrieveAccessToken(requestToken, verifier) match {
        case Right(t) => { // success
          TwitterLogic.upsertUserProfile(twitter, RequestToken(t.token, t.secret)).map { _ =>
            Redirect(routes.SettingController.showSetting)
          }
        }
        case Left(e) => {
          Future.successful(InternalServerError(e.getMessage))
        }
      }
    }
  }
}
