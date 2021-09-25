lazy val commonSettings = Seq(
  scalaVersion := "2.12.15",
  crossScalaVersions := Seq("2.11.12", "2.12.15"),
  scalacOptions ++= Seq("-encoding", "UTF-8") ++
    (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) =>
        Seq("-deprecation", "-feature", "-target:jvm-1.8") ++
          (if (n == 11) Seq("-Ybackend:GenBCode") else Nil)
      case _ => Nil
    }),
  updateOptions := updateOptions.value.withGigahorse(false)
)

val playVersion = "2.6.17"
val playJsonVersion = "2.6.9"

val specs2 = "org.specs2" %% "specs2-core" % "4.0.1" % Test

lazy val core = project
  .settings(
    commonSettings ++ Seq(
      name := "couchbase-scala",
      libraryDependencies ++= specs2 +: Seq(
        "com.couchbase.client" % "java-client" % "2.7.20",
        "javax.inject" % "javax.inject" % "1",
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.5.0",
        "com.typesafe.play" %% "play-json" % playJsonVersion,
        "com.typesafe" % "config" % "1.3.3",
        "com.google.inject" % "guice" % "4.2.0" % Test
      )
    )
  )

lazy val play = project
  .settings(
    commonSettings ++ Seq(
      name := "couchbase-play",
      libraryDependencies ++= specs2 +: Seq(
        "com.typesafe.play" %% "play" % playVersion,
        "com.typesafe.play" %% "play-guice" % playVersion
      )
    )
  )
  .dependsOn(core)

lazy val `couchbase-scala-root` = (project in file("."))
  .settings(commonSettings)
  .settings(
    publish / skip := true,
  )
  .aggregate(play, core)

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
