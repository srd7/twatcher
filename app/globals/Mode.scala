package twatcher

package object mode {
  sealed trait Mode

  object Batch extends Mode
  object Server extends Mode
  object Setting extends Mode
  object Default extends Mode

  object Mode {
    def apply(mode: String): Mode = mode match {
      case "batch"   => Batch
      case "server"  => Server
      case "setting" => Setting
      case _         => Default
    }
  }
}
