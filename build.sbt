val scala211 = "2.11.12"
val scala212 = "2.12.15"
val scala213 = "2.13.6"
val scala3 = "3.0.2"

val specs2Version = scalaBinaryVersion {
  case "2.11" => "4.10.6"
  case "3"    => "5.0.0-RC-11"
  case _      => "4.12.12"
}

val playJsonVersion = scalaBinaryVersion {
  case "2.11" | "2.12" => "2.6.9"
  case _               => "2.10.0-RC5"
}

lazy val scalacSetting = scalacOptions ++=
  Seq("-encoding", "UTF-8", "-deprecation", "-feature") ++
    (scalaBinaryVersion.value match {
      case "2.11" => Seq("-target:jvm-1.8", "-Ybackend:GenBCode")
      case "2.12" => Seq("-target:jvm-1.8")
      case _      => Nil
    })

lazy val `couchbase-scala` = projectMatrix
  .in(file("core"))
  .jvmPlatform(
    scalaVersions = Seq(scala211, scala212, scala213, scala3),
    settings = Seq(
      scalacSetting,
      libraryDependencies ++= Seq(
        "com.couchbase.client" % "java-client" % "2.7.20",
        "javax.inject" % "javax.inject" % "1",
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.5.0",
        "com.typesafe.play" %% "play-json" % playJsonVersion.value,
        "com.typesafe" % "config" % "1.3.3",
        "com.google.inject" % "guice" % "4.2.0" % Test,
        "org.specs2" %% "specs2-core" % specs2Version.value % Test,
      ),
    )
  )

lazy val play26 = ConfigAxis("_2_6", "-play2.6")
lazy val play28 = ConfigAxis("_2_8", "-play2.8")
lazy val playVersion = settingKey[String]("playVersion")
lazy val playDeps = libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % playVersion.value,
  "com.typesafe.play" %% "play-guice" % playVersion.value,
  "org.specs2" %% "specs2-core" % specs2Version.value % Test,
)

lazy val `couchbase-play` = projectMatrix
  .in(file("play"))
  .customRow(
    scalaVersions = Seq(scala211, scala212),
    axisValues = Seq(play26, VirtualAxis.jvm),
    settings = Seq(
      playVersion := "2.6.17",
      moduleName := name.value + "_2_6",
    ),
  )
  .customRow(
    scalaVersions = Seq(scala212, scala213),
    axisValues = Seq(play28, VirtualAxis.jvm),
    settings = Seq(
      playVersion := "2.8.8",
      moduleName := name.value,
    ),
  )
  .settings(scalacSetting, playDeps)
  .dependsOn(`couchbase-scala`)

// only aggregating project
lazy val `couchbase-scala-root` = (project in file("."))
  .settings(
    publish / skip := true,
    publishLocal / skip := true,
  )
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
