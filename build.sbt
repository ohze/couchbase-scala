val playJsonVersion = scalaBinaryVersion {
  case "2.11" | "2.12" => "2.6.14"
  case _               => "2.10.0-RC5"
}

val configVersion = scalaBinaryVersion {
  case "2.11" | "2.12" => "1.3.4"
  case _               => "1.4.1"
}

val oldCompat = scalaBinaryVersion.zip(playAxis.?) {
  case ("2.13" | "3", _) | (_, Some(`play27` | `play28`)) => false
  case _                                                  => true
}

lazy val mimaSetting = Seq(
  versionPolicyFirstVersion := Some(
    if (oldCompat.value) "7.4.5" else "9.0.0"
  ),
  versionPolicyPreviousVersions ++= {
    if (oldCompat.value) Seq("7.4.5", "9.0.0")
    else Seq("9.0.0")
  },
  mimaPreviousArtifacts := mimaPreviousArtifacts.value.map { m =>
    import coursier.version.Version
    if (Version(m.revision) >= Version("9.0.0")) m
    else m.withName(name.value)
  },
)

lazy val `couchbase-scala` = projectMatrix
  .in(file("core"))
  .jvmPlatform(scalaVersions = Seq(scala211, scala212, scala213, scala3))
  .settings(
    libraryDependencies ++= Seq(
      "com.couchbase.client" % "java-client" % "2.7.22",
      "javax.inject" % "javax.inject" % "1",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.6.0",
      "com.typesafe.play" %% "play-json" % playJsonVersion.value,
      "com.typesafe" % "config" % configVersion.value,
      "com.google.inject" % "guice" % "4.2.3" % Test,
    ) ++ specs2("-core").value,
    mimaSetting,
    libraryDependencySchemes ++= Seq(
      "org.scala-lang" % "scala3-library_3" % "semver-spec", // 3.0.2 -> 3.1.0
    ),
  )

lazy val `couchbase-play` = projectMatrix
  .in(file("play"))
  .playAxis(play26, Seq(scala211, scala212))
  .playAxis(play27, Seq(scala211, scala212, scala213))
  .playAxis(play28, Seq(scala212, scala213))
  .settings(
    mimaSetting,
    libraryDependencySchemes ++= Seq(
      "com.google.guava" % "guava" % "always", // change from 22.0 to 23.6.1-jre
      "com.sandinh" %% "couchbase-scala" % "always", // change from 7.4.5 to 9.x
      "com.typesafe" %% "ssl-config-core" % "always", // change from 0.2.2 to 0.3.8
      "org.scala-lang.modules" %% "scala-parser-combinators" % "semver-spec", // change from 1.0.6 to 1.1.2
    ),
    libraryDependencies ++=
      play("play", "guice").value ++ specs2("-core").value,
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
