organization := "com.sandinh"

name := "couchbase-scala"

version := "2.0.0"

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.11.4", "2.10.4")

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
  "com.couchbase.client"      %  "java-client"        % "2.0.1",
  "io.reactivex"              %% "rxscala"            % "0.22.0" exclude("io.reactivex", "rxjava"),
  "com.google.inject"         % "guice"               % "3.0",
  "com.typesafe.play"         %% "play-json"          % "2.3.6",
  "org.specs2"                %% "specs2"             % "2.4.9"     % "test"
)
