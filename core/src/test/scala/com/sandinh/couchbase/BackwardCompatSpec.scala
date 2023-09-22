package com.sandinh.couchbase

import com.typesafe.config.Config
import javax.inject.Inject
import scala.util.Random
import scala.sys.process._

class BackwardCompatSpec extends GuiceSpecBase {
  @Inject private var conf: Config = _
  private lazy val cp = conf.getString("compat-test.classpath")

  private def run(args: String) =
    s"java -cp $cp com.sandinh.couchbase.Main $args".!!.trim

  "counter" should {
    def rndKv() =
      ("compat" + Random.nextLong(), Random.nextLong().abs + 1)
    "backward compat: new set, old get" in {
      val (k, v) = rndKv()
      cb.bk1.counter(k, 0, v).map(_.content) must beEqualTo(v).await
      run(s"get counter $k") === v.toString
    }
    "backward compat: old set, new get" in {
      val (k, v) = rndKv()
      run(s"set counter $k $v") === v.toString
      cb.bk1.getCounter(k) must beEqualTo(v).await
    }
  }

  "CompatString" should {
    def rndKv() =
      ("compat" + Random.nextLong(), "" + Random.nextLong())
    "backward compat: new set, old get" in {
      val (k, v) = rndKv()
      cb.bk1.upsert(k, v).map(_.cas) must be_>(0L).await
      run(s"get CompatString $k") === v
    }
    "backward compat: old set, new get" in {
      val (k, v) = rndKv()
      run(s"set CompatString $k $v") === v
      cb.bk1.getT[String](k) must beEqualTo(v).await

      run(s"set String $k $v") === v
      cb.bk1.getT[String](k) must beEqualTo(v).await
    }
  }
}
