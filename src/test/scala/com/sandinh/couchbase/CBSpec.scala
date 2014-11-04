/** @author giabao
  * created: 2013-10-22 10:23
  * (c) 2011-2013 sandinh.com */
package com.sandinh.couchbase

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.{JsonArray, JsonObject}
import com.sandinh.couchbase.document.JsDocument
import org.specs2.time.NoTimeConversions
import play.api.libs.json.Json
import scala.concurrent.duration._
import com.sandinh.rx.Scala._

class CBSpec extends GuiceSpecBase with NoTimeConversions {
  sequential
  val timeout = 5.seconds
  val key = "test_key"

  def jsGet = cb.get[JsDocument](key).map(_.content.as[Trophy]).toFuture must beEqualTo(Trophy.t1).await(timeout = timeout)
  def jsonGet = cb.get[JsonDocument](key).toFuture.map(doc => Json.parse(doc.content.toString).as[Trophy]) must beEqualTo(Trophy.t1).await(timeout = timeout)
  def jsUpset = cb.upsert(JsDocument(key, Trophy.t1)).toFuture.map(_.id) must beEqualTo(key).await(timeout = timeout)
  def jsonUpset = {
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
    cb.upsert(JsonDocument.create(key, js)).toFuture.map(_.id) must beEqualTo(key).await(timeout = timeout)
  }

  "CB" should {
    "upsert using JsTranscoder then get using JsonTranscoder/ JsTranscoder" in {
      jsUpset
      jsonGet
      jsGet
    }
    "upsert using JsonTranscoder then get using JsonTranscoder/ JsTranscoder" in {
      jsonUpset
      jsonGet
      jsGet
    }
    "read" in {
      cb.read[Trophy](key).toFuture must beEqualTo(Trophy.t1).await(timeout = timeout)
    }
  }
}
