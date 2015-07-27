package twatcher.twitter

class TwitterException(msg: String, code: Int, parent: Throwable = null) extends RuntimeException(msg, parent)
