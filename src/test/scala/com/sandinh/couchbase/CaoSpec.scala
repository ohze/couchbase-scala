package com.sandinh.couchbase

import javax.inject.Inject
import com.sandinh.couchbase.access.JsCao1
import com.sandinh.rx.Implicits._

class CaoSpec extends GuiceSpecBase {
  @Inject private var trophyCao: TrophyCao = null

  val username = "giabao"
  "Cao" should {
    "success set & get" in {
      trophyCao.set(username, Trophy.t1).toFuture must beEqualTo(Trophy.t1).await
      trophyCao.get(username).toFuture must beEqualTo(Trophy.t1).await
    }
  }
}

import javax.inject._

@Singleton
class TrophyCao @Inject() (cb: CB) extends JsCao1[Trophy, String] {
  protected def key(username: String) = "test_cao_" + username
  protected def bucket = cb.bk1
}