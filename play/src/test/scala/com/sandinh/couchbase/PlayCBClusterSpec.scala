package com.sandinh.couchbase

import org.specs2.mutable.Specification
import play.api.inject.guice.GuiceApplicationBuilder

class PlayCBClusterSpec extends Specification {
  "PlayCBCluster" >> {
    val app = new GuiceApplicationBuilder().build()

    app.configuration.getOptional[String]("com.sandinh.couchbase.buckets.bk1.name") must beSome("fodi")
    app.configuration.get[Seq[String]]("play.modules.enabled") must contain("com.sandinh.couchbase.PlayModule")

    app.injector.instanceOf[CBCluster] must beAnInstanceOf[PlayCBCluster]
  }
}
