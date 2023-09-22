package com.sandinh.couchbase

import javax.inject._

@Singleton
class CB @Inject() (val cluster: CBCluster) {
  lazy val bk1 = cluster.bucket("fodi")
  lazy val bk2 = cluster.bucket("acc")
}
