package com.sandinh.couchbase

import javax.inject._
import com.sandinh.couchbase.transcoder.JsTranscoder
import com.typesafe.config.Config

@Singleton
class CB @Inject() (config: Config, val cluster: CBCluster) {
  lazy val bk1 = cluster.openBucket("bk1", JsTranscoder)
  lazy val bk2 = cluster.openBucket("acc")
}
