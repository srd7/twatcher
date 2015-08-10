package twatcher.twitter

object TwitterUris {
  final val USER_TIMELINE = "https://api.twitter.com/1.1/statuses/user_timeline.json"
  final val SELF_PROFILE = "https://api.twitter.com/1.1/account/verify_credentials.json"
  final val FAVORITES_LIST = "https://api.twitter.com/1.1/favorites/list.json"
  final val FRIENDS_IDS = "https://api.twitter.com/1.1/friends/ids.json"
  final val FOLLOWERS_IDS = "https://api.twitter.com/1.1/followers/ids.json"

  final val STATUSES_UPDATE = "https://api.twitter.com/1.1/statuses/update.json"
  final def STATUSES_DESTROY(id: Long) = s"https://api.twitter.com/1.1/statuses/destroy/${id}.json"
  final val UPDATE_PROFILE = "https://api.twitter.com/1.1/account/update_profile.json"
  final val FAVORITES_DESTROY = "https://api.twitter.com/1.1/favorites/destroy.json"
  final val BLOCKS_CREATE = "https://api.twitter.com/1.1/blocks/create.json"
  final val BLOCKS_DESTROY = "https://api.twitter.com/1.1/blocks/destroy.json"

  final val OAUTH_AUTHORIZE     = "https://api.twitter.com/oauth/authorize"
  final val OAUTH_ACCESS_TOKEN  = "https://api.twitter.com/oauth/access_token"
  final val OAUTH_REQUEST_TOKEN = "https://api.twitter.com/oauth/request_token"
}
