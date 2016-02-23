package com.sandinh.couchbase

import javax.inject.Inject
import com.couchbase.client.java.document.{JsonStringDocument, StringDocument}
import com.couchbase.client.java.error.TranscodingException
import com.sandinh.couchbase.document.CompatStringDocument
import com.typesafe.config.Config

class CompatStringSpec extends GuiceSpecBase {
  @Inject private var config: Config = null
  lazy val bk1Compat = {
    val cluster = new CBCluster(config)
    cluster.openBucket("bk1", legacyEncodeString = false)
  }

  val id = "test_CompatStringSpec"
  val s = "ab!?-'yf89da3\"\"2$^$\""

  def stringSet(idSuffix: Int) = cb.bk1.upsert(StringDocument.create(id + idSuffix, s)).map(_.content)
  def stringGet(idSuffix: Int) = cb.bk1.get[StringDocument](id + idSuffix).map(_.content)

  def jsonStringSet(idSuffix: Int) = cb.bk1.upsert(JsonStringDocument.create(id + idSuffix, s)).map(_.content)
  def jsonStringGet(idSuffix: Int) = cb.bk1.get[JsonStringDocument](id + idSuffix).map(_.content)

  def compatStringSet(idSuffix: Int) = cb.bk1.upsert(new CompatStringDocument(id + idSuffix, s)).map(_.content)
  def compatStringGet(idSuffix: Int) = cb.bk1.get[CompatStringDocument](id + idSuffix).map(_.content)

  "String transcoders" should {
    "1. set String get JsonString throw TranscodingException" in {
      stringSet(1) must beEqualTo(s).await
      jsonStringGet(1) must throwA[TranscodingException].await
    }

    "2. set JsonString get String throw TranscodingException" in {
      jsonStringSet(2) must beEqualTo(s).await
      stringGet(2) must throwA[TranscodingException].await
    }

    "3. set String get CompatString success" in {
      stringSet(3) must beEqualTo(s).await
      compatStringGet(3) must beEqualTo(s).await
    }

    "4. set JsonString get CompatString success" in {
      jsonStringSet(4) must beEqualTo(s).await
      compatStringGet(4) must beEqualTo(s).await
    }

    "5. set CompatString get JsonString success if use CompatStringTranscoder" in {
      bk1Compat.upsert(new CompatStringDocument(id + 5, s)).map(_.content) must beEqualTo(s).await
      bk1Compat.get[JsonStringDocument](id + 5).map(_.content) must beEqualTo(s).await
    }

    "6. set CompatString get String success if use LegacyStringTranscoder (default)" in {
      compatStringSet(6) must beEqualTo(s).await
      stringGet(6) must beEqualTo(s).await
    }

    "7. set CompatString get String fail if use CompatStringTranscoder" in {
      bk1Compat.upsert(new CompatStringDocument(id + 7, s)).map(_.content) must beEqualTo(s).await
      bk1Compat.get[StringDocument](id + 7).map(_.content) must throwA[TranscodingException].await
    }

    "8. set CompatString get JsonString fail if use LegacyStringTranscoder (default)" in {
      compatStringSet(8) must beEqualTo(s).await
      jsonStringGet(8) must throwA[TranscodingException].await
    }
  }
}
