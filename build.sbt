name := "twatcher"

val settings = Seq(
  name := "twatcher"
, version := "1.0.0"
, scalaVersion := "2.11.7"
, scalacOptions ++= Seq(
    "-unchecked", "-deprecation", "-feature"
  , "-encoding", "utf8"
  )
)

lazy val root = (
  project in file(".")
  enablePlugins PlayScala
  settings(settings)
  settings(
    libraryDependencies ++= Seq(
      ws
    )
  )
)
