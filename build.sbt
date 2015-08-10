name := "twatcher"

val settings = Seq(
  name := "twatcher"
, version := "1.1.2"
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
      jdbc
    , ws
    , evolutions
    , cache
    , "com.typesafe.slick" %% "slick" % "3.0.0"
    )
  )
)

// Add static script files to bin/ directory
mappings in Universal <++= (packageBin in Compile) map { jar =>
  val scriptsDir = new java.io.File("scripts/bin")
  scriptsDir.listFiles.toSeq.map { f =>
    f -> ("bin/" + f.getName)
  }
}

mappings in Universal <++= (packageBin in Compile) map { jar =>
  val scriptsDir = new java.io.File("scripts/public")
  scriptsDir.listFiles.toSeq.map { f =>
    f -> f.getName
  }
}

routesGenerator := InjectedRoutesGenerator

// Do not generate API doc
doc in Compile <<= target.map(_ / "none")
