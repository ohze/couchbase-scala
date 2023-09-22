package com.sandinh.couchbase

import com.couchbase.client.core.error.CasMismatchException
import javax.inject.Inject
import scala.concurrent.duration._
import scala.concurrent.Await

class OptimisticLocking extends GuiceSpecBase {
  @Inject private var trophyCao: TrophyCao = null

  "CBCluster" should {
    "pass optimistic-locking" in {
      val key = "test_optimistic_locking"
      trophyCao.upsert(key, Trophy.t1).map(_.cas) must be_>(0L).await
      val old: (Trophy, Long) =
        Await.result(trophyCao.getWithCAS(key), 2.second)
      val t2 = trophyCao.replace(key, Trophy.t2, old._2)
      val t3 = trophyCao.replace(key, Trophy.t3, old._2)
      t2.zip(t3) must throwAn[CasMismatchException].await
    }
  }
}
