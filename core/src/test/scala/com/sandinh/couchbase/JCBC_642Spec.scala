package com.sandinh.couchbase

import com.couchbase.client.scala.kv.UpsertOptions

import javax.inject.Inject
import com.typesafe.config.Config
import scala.concurrent.duration._
import com.couchbase.client.scala.codec.JsonSerializer.StringConvert

class JCBC_642Spec extends GuiceSpecBase {
  @Inject private[this] var cfg: Config = _

  "CBCluster" should {
    "pass JCBC-642" in {
      cfg must !==(null)
      println("load config from: " + cfg.origin().description())
      val cfgFile = getClass.getClassLoader.getResource("application.conf")
      cfg.origin().description() must contain(s"@ $cfgFile")
      val content = "test_value_JCBC_642"
      cb.bk1
        .upsert(
          "test_key_JCBC_642",
          content,
          UpsertOptions().expiry(10.seconds)
        )
        .flatMap { _ =>
          cb.bk2.upsert(
            "test_key_JCBC_642",
            content,
            UpsertOptions().expiry(10.seconds)
          )
        }
        .map(_.cas) must be_>(0L).await
      cfg must !==(null)
    }
  }
}
