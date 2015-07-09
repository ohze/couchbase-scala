package com.sandinh.couchbase

import javax.inject._

@Singleton
class CB @Inject() (val cluster: CBCluster) {
  lazy val bk1 = cluster.openBucket("bk1")
  lazy val bk2 = cluster.openBucket("acc")
}
