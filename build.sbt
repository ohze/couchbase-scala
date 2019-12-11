lazy val commonSettings = Seq(
  version := "8.0.0-SNAPSHOT",
  scalaVersion := "2.13.1",
  crossScalaVersions := Seq("2.13.1", "2.12.10"),
  organization := "com.sandinh",

  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature"),
)

val playVersion = "2.8.0-RC5"
val playJsonVersion = "2.8.0"

val specs2 = "org.specs2"        %% "specs2-core"  % "4.8.1" % Test
val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.29" % Test

lazy val core = (project in file("core"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-scala",
    libraryDependencies ++= Seq(specs2, slf4jSimple,
      "com.couchbase.client"      % "java-client"         % "2.7.11",
      "javax.inject"              % "javax.inject"        % "1",
      "com.typesafe.play"         %% "play-json"          % playJsonVersion,
      "com.typesafe"              % "config"              % "1.4.0", //same as akka-actor:2.6.0
      "com.google.inject"         % "guice"               % "4.2.2" % Test,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.2",
    )
  ))

lazy val play = (project in file("play"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-play",
    libraryDependencies ++= Seq(specs2, slf4jSimple,
      "com.typesafe.play" %% "play"       % playVersion,
      "com.typesafe.play" %% "play-guice" % playVersion
    )
  )).dependsOn(core)

lazy val `couchbase-scala-root` = (project in file("."))
  .settings(commonSettings)
  .settings(
    packagedArtifacts := Map.empty
  ).aggregate(play, core)
