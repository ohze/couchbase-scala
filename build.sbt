import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

val playVersion = "2.4.2" //require java 8

lazy val formatSettings = scalariformSettings ++ Seq(
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
//  .setPreference(ScaladocCommentsStopOnLastLine, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
  .setPreference(SpacesAroundMultiImports, false)
)

lazy val commonSettings = formatSettings ++ Seq(
  version := "7.1.1",
  scalaVersion := "2.11.7",
  organization := "com.sandinh",

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
  ),

  testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),

  resolvers ++= Seq(
    Resolver.typesafeRepo("releases"),
    Resolver.bintrayRepo("scalaz", "releases")
  )
)

lazy val core = (project in file("core"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-scala",
    libraryDependencies ++= Seq(
      "com.couchbase.client"      %  "java-client"        % "2.1.3",
      "javax.inject"              % "javax.inject"        % "1",
      "com.typesafe.play"         %% "play-json"          % playVersion,
      "com.google.inject"         % "guice"               % "4.0"       % "test",
      "org.specs2"                %% "specs2-junit"       % "3.6.2"     % "test",
      //update from rxjava 1.0.4 (transitive dep from com.couchbase.client:core-io:1.1.1)
      "io.reactivex" % "rxjava" % "1.0.12"
    )
  ))

lazy val play = (project in file("play"))
  .settings(commonSettings ++ Seq(
    name := "couchbase-play",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % playVersion
    )
  )).dependsOn(core)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    packagedArtifacts := Map.empty
  ).aggregate(play, core)
