lazy val commonSettings = Seq(
  version := "7.4.2",
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.11.11", "2.12.4"),
  organization := "com.sandinh",

  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-target:jvm-1.8"),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => Seq("-Ybackend:GenBCode")
    case _ => Nil
  })
)

val playVersion = "2.6.7"

val specs2 = "org.specs2"        %% "specs2-core"  % "4.0.1" % Test

lazy val core = (project in file("core"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-scala",
    libraryDependencies ++= specs2 +: Seq(
      "com.couchbase.client"      % "java-client"         % "2.5.2",
      "javax.inject"              % "javax.inject"        % "1",
      "com.typesafe.play"         %% "play-json"          % playVersion,
      "com.typesafe"              % "config"              % "1.3.2",
      "com.google.inject"         % "guice"               % "4.1.0" % Test
    )
  ))

lazy val play = (project in file("play"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-play",
    libraryDependencies ++= specs2 +: Seq(
      "com.typesafe.play" %% "play"       % playVersion,
      "com.typesafe.play" %% "play-guice" % playVersion
    )
  )).dependsOn(core)

lazy val `couchbase-scala-root` = (project in file("."))
  .settings(commonSettings)
  .settings(
    packagedArtifacts := Map.empty
  ).aggregate(play, core)
