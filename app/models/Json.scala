package twatcher.models

import play.api.libs.json.{Json => PlayJson, _}
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._

object Json {
  /**
   * Reads for twatcher.models.RequestToken
   */
  implicit final val requestTokenReads: Reads[RequestToken] = (
    (__ \ "screen_name").read[String] and
    (__ \ "token"      ).read[String] and
    (__ \ "secret"     ).read[String]
  )(RequestToken.apply _)

  /**
   * Writes for twatcher.models.RequestToken
   */
  implicit final val requestTokenWrites: Writes[RequestToken] = (
    (__ \ "screen_name").write[String] and
    (__ \ "token"      ).write[String] and
    (__ \ "secret"     ).write[String]
  )(unlift(RequestToken.unapply))

  case class JsonConfigItem(
    tokenList: List[RequestToken]
  , periodDay: Int
  )

  /**
   * Reads for conf/config.json
   */
  implicit final val jsonConfigReads: Reads[JsonConfigItem] = (
    (__ \ "token").read[List[RequestToken]] and
    (__ \ "period").read[Int]
  )(JsonConfigItem.apply _)
}
