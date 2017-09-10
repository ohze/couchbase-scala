lazy val commonSettings = Seq(
  version := "7.4.0",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  organization := "com.sandinh",

  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-target:jvm-1.8"),
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => Seq("-Ybackend:GenBCode")
    case _ => Nil
  })
)

val specs2 = "org.specs2"        %% "specs2-core"  % "3.9.5" % Test

lazy val core = (project in file("core"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-scala",
    libraryDependencies ++= specs2 +: Seq(
      "com.couchbase.client"      % "java-client"         % "2.5.0",
      "javax.inject"              % "javax.inject"        % "1",
      "com.typesafe.play"         %% "play-json"          % "2.6.3",
      "com.typesafe"              % "config"              % "1.3.1",
      "com.google.inject"         % "guice"               % "4.1.0" % Test
    )
  ))

lazy val play = (project in file("play"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-play",
    libraryDependencies ++= specs2 +: Seq(
      "com.typesafe.play" %% "play"       % "2.6.3",
      "com.typesafe.play" %% "play-guice" % "2.6.3"
    )
  )).dependsOn(core)

lazy val `couchbase-scala-root` = (project in file("."))
  .settings(commonSettings)
  .settings(
    packagedArtifacts := Map.empty
  ).aggregate(play, core)
