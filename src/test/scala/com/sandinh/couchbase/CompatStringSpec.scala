package com.sandinh.couchbase

import com.couchbase.client.java.document.{JsonStringDocument, StringDocument}
import com.couchbase.client.java.error.TranscodingException
import com.sandinh.couchbase.document.CompatStringDocument

class CompatStringSpec extends GuiceSpecBase {
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

    "5. set CompatString get JsonString success" in {
      compatStringSet(5) must beEqualTo(s).await
      jsonStringGet(5) must beEqualTo(s).await
    }
  }
}
