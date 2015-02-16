organization := "com.sandinh"

name := "couchbase-scala"

version := "6.2.0-SNAPSHOT"

scalaVersion := "2.11.5"

//see https://github.com/scala/scala/blob/2.11.x/src/compiler/scala/tools/nsc/settings/ScalaSettings.scala
scalacOptions ++= Seq("-encoding", "UTF-8"
  , "-target:jvm-1.7", "-deprecation", "-unchecked", "-feature"
  , "-optimise"
  ,"-Xfuture" //, "–Xverify", "-Xcheck-null"
  ,"-Ybackend:GenBCode"
  ,"-Ydelambdafy:method"
  ,"-Yinline-warnings" //, "-Yinline"
  ,"-Ywarn-dead-code", "-Ydead-code"
  ,"-Yclosure-elim"
  ,"-Ywarn-unused-import", "-Ywarn-numeric-widen"
  //`sbt doc` will fail if enable the following options!
  //,"nullary-unit", "nullary-override", "unsound-match", "adapted-args", "infer-any"
)

//@see https://github.com/etorreborre/specs2/issues/283
disablePlugins(plugins.JUnitXmlReportPlugin)

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console")

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "com.couchbase.client"      %  "java-client"        % "2.1.0",
  "javax.inject"              % "javax.inject"        % "1",
  "com.typesafe.play"         %% "play-json"          % "2.3.8",
  "com.google.inject"         % "guice"               % "3.0"       % "test",
  "org.specs2"                %% "specs2"             % "2.4.16"    % "test"
)
