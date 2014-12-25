organization := "com.sandinh"

name := "couchbase-scala"

version := "4.0.1"

scalaVersion := "2.11.4"

scalacOptions ++= Seq(
  "-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature", //"-optimise",
  "-Xmigration", "-Xfuture", //"–Xverify", "-Xcheck-null", "-Ystatistics",
  "-Yinline-warnings", //"-Yinline",
  "-Ywarn-dead-code", "-Ydead-code"
)

//@see https://github.com/etorreborre/specs2/issues/283
lazy val root = (project in file(".")) disablePlugins plugins.JUnitXmlReportPlugin

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console")

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "com.couchbase.client"      %  "java-client"        % "2.0.2",
  "io.reactivex"              % "rxjava"              % "1.0.3",
  "javax.inject"              % "javax.inject"        % "1",
  "com.typesafe.play"         %% "play-json"          % "2.3.7",
  "com.google.inject"         % "guice"               % "3.0"       % "test",
  "org.specs2"                %% "specs2"             % "2.4.15"    % "test"
)
