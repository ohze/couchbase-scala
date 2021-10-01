val specs2Version = scalaBinaryVersion {
  case "2.11" => "4.10.6"
  case "3"    => "5.0.0-RC-11"
  case _      => "4.12.12"
}
val specs2 = specs2Version("org.specs2" %% "specs2-core" % _ % Test)

val playJsonVersion = scalaBinaryVersion {
  case "2.11" | "2.12" => "2.6.14"
  case _               => "2.10.0-RC5"
}

val configVersion = scalaBinaryVersion {
  case "2.11" | "2.12" => "1.3.4"
  case _               => "1.4.1"
}

lazy val mimaSetting =
  mimaPreviousArtifacts := (scalaBinaryVersion.value match {
    case "2.11" | "2.12" => Set(organization.value %% name.value % "7.4.5")
    case _ => Set.empty // TODO update after releasing first version
    // Set(organization.value %% moduleName.value % "9.0.0")
  })

lazy val `couchbase-scala` = projectMatrix
  .in(file("core"))
  .jvmPlatform(
    scalaVersions = Seq(scala211, scala212, scala213, scala3),
    settings = Seq(
      libraryDependencies ++= Seq(
        "com.couchbase.client" % "java-client" % "2.7.20",
        "javax.inject" % "javax.inject" % "1",
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.5.0",
        "com.typesafe.play" %% "play-json" % playJsonVersion.value,
        "com.typesafe" % "config" % configVersion.value,
        "com.google.inject" % "guice" % "4.2.3" % Test,
        specs2.value,
      ),
      mimaSetting,
      Compile / doc / scalacOptions -= "-Xfatal-warnings",
    )
  )

import PlayAxis._, VirtualAxis.jvm

lazy val `couchbase-play` = projectMatrix
  .in(file("play"))
  .customRow(
    scalaVersions = Seq(scala211, scala212),
    axisValues = Seq(play26, jvm),
    settings = Seq(
      moduleName := name.value + "_2_6",
      libraryDependencies ++= play26.deps :+ specs2.value,
    ),
  )
  .customRow(
    scalaVersions = Seq(scala211, scala212, scala213),
    axisValues = Seq(play27, jvm),
    settings = Seq(
      moduleName := name.value + "_2_7",
      libraryDependencies ++= play27.deps :+ specs2.value,
    ),
  )
  .customRow(
    scalaVersions = Seq(scala212, scala213),
    axisValues = Seq(play28, jvm),
    settings = Seq(
      moduleName := name.value,
      libraryDependencies ++= play28.deps :+ specs2.value,
    ),
  )
  .settings(mimaSetting)
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

// opens java.base/java.lang for java 16+
// to workaround error:
// InaccessibleObjectException: Unable to make protected final java.lang.Class
//  java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
//  throws java.lang.ClassFormatError accessible:
//  module java.base does not "opens java.lang" to unnamed module @42a6eabd (ReflectUtils.java:61)
lazy val javaVersion: Int = scala.sys
  .props("java.specification.version")
  .split('.')
  .dropWhile(_ == "1")
  .head
  .toInt
ThisBuild / Test / fork := javaVersion >= 16
ThisBuild / Test / javaOptions := {
  if (javaVersion < 16) Nil
  else Seq("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}
