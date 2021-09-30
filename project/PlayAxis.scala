import sbt._

case class PlayAxis private (version: String) extends VirtualAxis.WeakAxis {
  private def minorVersion = version match {
    case VersionNumber(Seq(2, n, _), _, _) => n
    case _ => sys.error(s"invalid play version $version")
  }

  val idSuffix = s"_2_$minorVersion"
  val directorySuffix = s"-play2.$minorVersion"

  def component(id: String): ModuleID = "com.typesafe.play" %% id % version

  def deps = Seq(component("play"), component("play-guice"))
}

object PlayAxis {
  val (play26, play27, play28) =
    (PlayAxis("2.6.25"), PlayAxis("2.7.9"), PlayAxis("2.8.8"))
}
