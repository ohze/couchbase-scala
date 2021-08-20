package com.sandinh.couchbase

import javax.inject.Inject

import com.couchbase.client.java.document.JsonStringDocument
import com.typesafe.config.Config

class JCBC_642Spec extends GuiceSpecBase {
  @Inject private[this] var cfg: Config = _

  "CBCluster" should {
    "pass JCBC-642" in {
      cfg must_!= null
      println("load config from: " + cfg.origin().description())
      val cfgFile = {
        val loader = classOf[JCBC_642Spec].getClassLoader
        loader.getResource("application.conf")
      }
      cfg.origin().description() must contain(s"@ $cfgFile")
      val content = "test_value_JCBC_642"
      val doc = JsonStringDocument.create("test_key_JCBC_642", 10000, content)
      cb.bk1.upsert(doc)
        .flatMap { d => cb.bk2.upsert(d) }
        .map(_.content) must beEqualTo(content).await
      cfg must_!= null
    }
  }
}
