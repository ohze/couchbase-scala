package com.sandinh.couchbase

import javax.inject._
import com.typesafe.config.Config

@Singleton
class CB @Inject() (config: Config, val cluster: CBCluster) {
  lazy val bk1 = cluster.openBucket("bk1")
  lazy val bk2 = cluster.openBucket("acc")
}
