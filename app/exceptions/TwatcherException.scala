package twatcher.exceptions

class TwatcherException(msg: String, parent: Throwable = null) extends RuntimeException(msg, parent)

class InsertZipTweetException(msg: String, parent: Throwable = null) extends TwatcherException(msg, parent)
