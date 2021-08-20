import sbt._
import sbt.Keys._
import xerial.sbt.Sonatype.SonatypeKeys._

object MySonatype {
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/ohze/couchbase-scala")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/ohze/couchbase-scala"),
        "scm:git@github.com:ohze/couchbase-scala.git"
      )
    ),
    developers := List(
      Developer("giabao", "Bùi Việt Thành", "thanhbv@sandinh.net", url("https://sandinh.com"))
    )
  )
}
