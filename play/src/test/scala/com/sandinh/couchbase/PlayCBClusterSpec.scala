package com.sandinh.couchbase

import org.specs2.mutable.Specification
import play.api.inject.guice.GuiceApplicationBuilder

class PlayCBClusterSpec extends Specification {
  "PlayCBCluster" >> {
    val app = new GuiceApplicationBuilder().build()

    app.configuration.getString("com.sandinh.couchbase.buckets.bk1.name") must beSome("fodi")
    app.configuration.getStringSeq("play.modules.enabled").get must contain("com.sandinh.couchbase.PlayModule")

    app.injector.instanceOf[CBCluster] must beAnInstanceOf[PlayCBCluster]
  }
}
