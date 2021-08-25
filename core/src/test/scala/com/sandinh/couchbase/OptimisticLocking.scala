package com.sandinh.couchbase

import com.couchbase.client.java.error.CASMismatchException

import java.time.Duration
import javax.inject.Inject
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class OptimisticLocking extends GuiceSpecBase {
  @Inject private var trophyCao: TrophyCao = null

  "CBCluster" should {
    "pass optimistic-locking" in {
      val key = "test_optimistic_locking"
      trophyCao.set(key, Trophy.t1).map(_.content.as[Trophy]) must beEqualTo(Trophy.t1).await
      val old: (Trophy, Long) = Await.result(trophyCao.getWithCAS(key), 2.second)
      val t2 = trophyCao.updateWithCAS(key, Trophy.t2, old._2)
      val t3 = trophyCao.updateWithCAS(key, Trophy.t3, old._2)
      RichFuture.zip(t2, t3) must throwAn[CASMismatchException].await
    }
  }
}

object RichFuture {
  def zip[A, B](a: Future[A], b: Future[B])(implicit ec: ExecutionContext): Future[(A, B)] =
    Future.sequence(Seq(a, b)).map(_ => (a.value.get.get, b.value.get.get))
}
