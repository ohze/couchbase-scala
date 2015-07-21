package com.sandinh.couchbase

import com.sandinh.PlayAlone
import org.specs2.mutable.Specification
import play.api.Play

class PlayCBClusterSpec extends Specification {
  "PlayCBCluster" >> {
    PlayAlone.start()

    Play.current.configuration.getString("com.sandinh.couchbase.buckets.bk1.name") must beSome("fodi")
    Play.current.configuration.getStringSeq("play.modules.enabled").get must contain("com.sandinh.couchbase.PlayModule")

    Play.current.injector.instanceOf[CBCluster] must beAnInstanceOf[PlayCBCluster]
  }
}
