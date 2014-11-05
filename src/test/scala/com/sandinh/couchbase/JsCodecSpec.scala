/** @author giabao
  * created: 2013-10-22 10:23
  * (c) 2011-2013 sandinh.com */
package com.sandinh.couchbase

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.{JsonArray, JsonObject}
import com.sandinh.couchbase.document.JsDocument
import play.api.libs.json.Json
import com.sandinh.rx.Implicits._

class JsCodecSpec extends GuiceSpecBase {
  val key = "test_key"

  def jsGet = cb.bk1.get[JsDocument](key).map(_.content.as[Trophy]).toFuture must beEqualTo(Trophy.t1).await
  def jsonGet = cb.bk1.get[JsonDocument](key).toFuture.map(doc => Json.parse(doc.content.toString).as[Trophy]) must beEqualTo(Trophy.t1).await
  def jsSet = cb.bk1.upsert(JsDocument(key, Trophy.t1)).toFuture.map(_.id) must beEqualTo(key).await
  def jsonSet = {
    import Trophy.t1
    val arr = JsonArray.empty()
    for (d <- t1.d) {
      val a2 = JsonArray.empty()
      for (i <- d) a2.add(i)
      arr.add(a2)
    }
    val js = JsonObject.empty()
      .put("n", t1.n)
      .put("d", arr)
    cb.bk1.upsert(JsonDocument.create(key, js)).toFuture.map(_.id) must beEqualTo(key).await
  }

  "JsCodec" should {
    "upsert using JsTranscoder then get using JsonTranscoder/ JsTranscoder" in {
      jsSet
      jsonGet
      jsGet
    }
    "upsert using JsonTranscoder then get using JsonTranscoder/ JsTranscoder" in {
      jsonSet
      jsonGet
      jsGet
    }
  }
}
