package twatcher.twitter

import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import java.util.Date

object TwitterReads {
  /**
   * Reads for java.util.Date
   * The response string is like "Wed Sep 05 00:37:15 +0000 2012"
   */
  private[this] final val dateReads: Reads[Date] = new Reads[Date] {
    import java.text.{ParseException, ParsePosition, SimpleDateFormat}
    val error = JsError(
      Seq(JsPath() -> Seq(ValidationError("error.expected.date.string")))
    )
    def reads(json: JsValue): JsResult[Date] = json match {
      case JsString(str) => {
        val format = new SimpleDateFormat("dd HH:mm:ss Z yyyy")
        // Throw exception if not a format of date
        format.setLenient(false)
        try {
          JsSuccess(format.parse(str, new ParsePosition(8)))
        } catch {
          case e: java.text.ParseException => error
        }
      }
      case _ => error
    }
  }

  /**
   * Reads for TwitterError from JSON
   */
  implicit final val twitterErrorReads: Reads[TwitterError] = (
    (__ \ "message").read[String] and
    (__ \ "code").read[Int]
  )(TwitterError.apply _)

  /**
   * Reads for User
   */
  implicit final val userReads: Reads[User] = (
    (__ \ "id").read[Long] and
    (__ \ "name").read[String] and
    (__ \ "screen_name").read[String] and
    (__ \ "location").read[String] and
    (__ \ "description").read[String] and
    (__ \ "protected").read[Boolean] and
    (__ \ "followers_count").read[Long] and
    (__ \ "friends_count").read[Long] and
    (__ \ "created_at").read[Date](dateReads) and
    (__ \ "favourites_count").read[Long] and
    (__ \ "statuses_count").read[Long] and
    (__ \ "profile_background_color").read[String] and
    (__ \ "profile_background_image_url").read[String] and
    (__ \ "profile_image_url").read[String] and
    (__ \ "profile_link_color").read[String] and
    (__ \ "profile_text_color").read[String]
  )(User.apply _)

  /**
   * Reads for single Tweet
   */
  implicit final val tweetReads: Reads[Tweet] = (
    (__ \ "created_at").read[Date](dateReads) and
    (__ \ "id").read[Long] and
    (__ \ "text").read[String] and
    (__ \ "source").read[String] and
    (__ \ "in_reply_to_screen_name").readNullable[String] and
    (__ \ "in_reply_to_status_id").readNullable[Long] and
    (__ \ "in_reply_to_user_id").readNullable[Long] and
    (__ \ "user").read[User]
  )(Tweet.apply _)
}
