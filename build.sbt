val specs2Version = scalaBinaryVersion {
  case "2.11" => "4.10.6"
  case "3"    => "5.0.0-RC-11"
  case _      => "4.12.12"
}
val specs2 = specs2Version("org.specs2" %% "specs2-core" % _ % Test)

val playJsonVersion = scalaBinaryVersion {
  case "2.11" | "2.12" => "2.6.14"
  case _               => "2.10.0-RC5"
}

val configVersion = scalaBinaryVersion {
  case "2.11" | "2.12" => "1.3.4"
  case _               => "1.4.1"
}

lazy val mimaSetting =
  mimaPreviousArtifacts := (scalaBinaryVersion.value match {
    case "2.11" | "2.12" => Set(organization.value %% name.value % "7.4.5")
    case _ => Set.empty // TODO update after releasing first version
    // Set(organization.value %% moduleName.value % "9.0.0")
  })

lazy val `couchbase-scala` = projectMatrix
  .in(file("core"))
  .jvmPlatform(
    scalaVersions = Seq(scala211, scala212, scala213, scala3),
    settings = Seq(
      libraryDependencies ++= Seq(
        "com.couchbase.client" % "java-client" % "2.7.20",
        "javax.inject" % "javax.inject" % "1",
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.5.0",
        "com.typesafe.play" %% "play-json" % playJsonVersion.value,
        "com.typesafe" % "config" % configVersion.value,
        "com.google.inject" % "guice" % "4.2.3" % Test,
        specs2.value,
      ),
      mimaSetting,
    )
  )

lazy val `couchbase-play` = projectMatrix
  .in(file("play"))
  .playAxis(play26, Seq(scala211, scala212))
  .playAxis(play27, Seq(scala211, scala212, scala213))
  .playAxis(play28, Seq(scala212, scala213))
  .settings(
    mimaSetting,
    libraryDependencies ++= play("play", "guice").value :+ specs2.value,
  )
  .dependsOn(`couchbase-scala`)

// only aggregating project
lazy val `couchbase-scala-root` = (project in file("."))
  .disablePlugins(MimaPlugin)
  .settings(skipPublish)
  .aggregate(`couchbase-play`.projectRefs ++ `couchbase-scala`.projectRefs: _*)

inThisBuild(
  Seq(
    versionScheme := Some("semver-spec"),
    developers := List(
      Developer(
        "thanhbv",
        "Bui Viet Thanh",
        "thanhbv@sandinh.net",
        url("https://sandinh.com")
      ),
      Developer(
        "vinhbt",
        "Bui The Vinh",
        "vinhbt@sandinh.net",
        url("https://sandinh.com")
      ),
      Developer(
        "thanhpv",
        "Phan Van Thanh",
        "thanhpv@sandinh.net",
        url("https://sandinh.com")
      ),
    ),
  )
)

// In Test code: com.sandinh.couchbase.GuiceSpecBase.setup
// We use Guice's injectMembers that inject value for the GuiceSpecBase's private var `_cb`
// using reflection which is deny by default in java 16+
inThisBuild(addOpensForTest())
