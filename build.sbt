lazy val commonSettings = Seq(
  version := "7.4.3",
  scalaVersion := "2.12.14",
  crossScalaVersions := Seq("2.11.12", "2.12.14"),
  organization := "com.sandinh",

  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-target:jvm-1.8"),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => Seq("-Ybackend:GenBCode")
    case _ => Nil
  })
)

val playVersion = "2.6.17"
val playJsonVersion = "2.6.9"

val specs2 = "org.specs2"        %% "specs2-core"  % "4.0.1" % Test

lazy val core = (project in file("core"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-scala",
    libraryDependencies ++= specs2 +: Seq(
      "com.couchbase.client"      % "java-client"         % "2.6.0",
      "javax.inject"              % "javax.inject"        % "1",
      "com.typesafe.play"         %% "play-json"          % playJsonVersion,
      "com.typesafe"              % "config"              % "1.3.3",
      "com.google.inject"         % "guice"               % "4.2.0" % Test
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
