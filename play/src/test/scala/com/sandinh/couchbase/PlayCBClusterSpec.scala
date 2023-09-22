package com.sandinh.couchbase

import org.specs2.mutable.Specification
import play.api.inject.guice.GuiceApplicationBuilder

class PlayCBClusterSpec extends Specification {
  "PlayCBCluster" >> {
    val app = new GuiceApplicationBuilder().build()
    val c = app.configuration
    c.get[String]("com.sandinh.couchbase.connectionString") mustNotEqual ""
    c.get[String]("com.sandinh.couchbase.user") === "cb"
    c.get[Seq[String]]("play.modules.enabled") must contain(
      "com.sandinh.couchbase.PlayModule"
    )

    app.injector.instanceOf[CBCluster] must beAnInstanceOf[PlayCBCluster]
  }
}
