package com.sandinh.couchbase

import com.couchbase.client.java.document.JsonStringDocument
import scala.concurrent.duration._

class JCBC_642Spec extends GuiceSpecBase {
  "CBCluster" should {
    "pass JCBC-642" in {
      val content = "test_value_JCBC_642"
      val doc = JsonStringDocument.create("test_key_JCBC_642", 10000, content)
      cb.bk1.flatMap { bk =>
        bk.upsert(doc).flatMap { d =>
          cb.bk2.flatMap(_.upsert(d))
        }
      }.timeout(5.seconds)
        .toBlocking
        .single
        .content === content
    }
  }
}
