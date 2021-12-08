val emptyDoc = Compile / packageDoc := {
  val f = (Compile / packageDoc / artifactPath).value
  IO.touch(f)
  f
}

lazy val `couchbase-scala` = projectMatrix
  .in(file("core"))
  .configAxis(config13, Seq(scala212, scala213))
  .configAxis(config14, Seq(scala212, scala213))
  .settings(
    libraryDependencies ++= Seq(
      "com.couchbase.client" %% "scala-client" % "1.2.3",
      "javax.inject" % "javax.inject" % "1",
      "com.typesafe.play" %% "play-json" % "2.10.0-RC5",
      "com.typesafe" % "config" % configAxis.value.version,
      "com.google.inject" % "guice" % "5.0.1" % Test,
    ) ++ specs2("-core").value,
    emptyDoc,
  )

lazy val `couchbase-play` = projectMatrix
  .in(file("play"))
  .playAxis(
    play26,
    Seq(scala212),
    _.dependsOn(`couchbase-scala`.finder(config13)(scala212))
  )
  .playAxis(
    play27,
    Seq(scala212),
    _.dependsOn(`couchbase-scala`.finder(config13)(scala212))
  )
  .playAxis(
    play27,
    Seq(scala213),
    _.dependsOn(`couchbase-scala`.finder(config13)(scala213))
  )
  .playAxis(
    play28,
    Seq(scala212),
    _.dependsOn(`couchbase-scala`.finder(config14)(scala212))
  )
  .playAxis(
    play28,
    Seq(scala213),
    _.dependsOn(`couchbase-scala`.finder(config14)(scala213))
  )
  .settings(
    libraryDependencies ++= play("play", "guice").value ++
      specs2("-core").value ++
      Seq(
        "ch.qos.logback" % "logback-classic" % "1.2.7" % Test,
      ),
    emptyDoc,
  )

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

inThisBuild(
  // In Test code: com.sandinh.couchbase.GuiceSpecBase.setup
  // We use Guice's injectMembers that inject value for the GuiceSpecBase's private var `_cb`
  // using reflection which is deny by default in java 16+
  addOpensForTest() ++ Seq(
    Test / fork := true,
    Test / javaOptions += {
      val host = java.net.InetAddress.getLocalHost.getHostAddress
      s"-Dcom.sandinh.couchbase.connectionString=$host"
    },
  )
)
