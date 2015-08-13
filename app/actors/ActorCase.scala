package twatcher.actors

import twatcher.models.{Account, Script}

case class Watch()

case class ScriptList(value: List[Script])
case class ScriptMessage(script: Script)

case class AccountList(value: List[Account])
case class AccountMessage(account: Account)

case class Goodbye(account: Account)
case class TweetDelete(account: Account)
case class FavoriteDelete(account: Account)
case class UpdateProfile(account: Account)

case class TwitterExecuteFinish()

case class IsRunning()
case class RunningStatus(value: Boolean)
case class Exit()
