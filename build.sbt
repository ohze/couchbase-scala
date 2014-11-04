organization := "com.sandinh"

name := "couchbase-scala"

version := "1.0.0"

scalaVersion := "2.11.4"

crossScalaVersions := Seq(
  "2.11.4",
  "2.10.4"
)

scalacOptions ++= Seq(
  "-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature", //"-optimise",
  "-Xmigration", "-Xfuture", //"–Xverify", "-Xcheck-null", "-Ystatistics",
  "-Yinline-warnings", //"-Yinline",
  "-Ywarn-dead-code", "-Ydead-code"
)

javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:unchecked", "-Xlint:deprecation")

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "com.couchbase.client"      %  "java-client"        % "2.0.1",
  "io.reactivex"              %% "rxscala"            % "0.22.0" exclude("io.reactivex", "rxjava"),
  "com.google.inject"         % "guice"               % "3.0",
  "com.typesafe.play"         %% "play-json"          % "2.3.6",
  "org.specs2"                %% "specs2"             % "2.4.9"     % "test"
)
