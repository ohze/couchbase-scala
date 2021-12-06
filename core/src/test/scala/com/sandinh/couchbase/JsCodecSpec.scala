/** @author giabao
  * created: 2013-10-22 10:23
  * (c) 2011-2013 sandinh.com
  */
package com.sandinh.couchbase

import com.couchbase.client.scala.json.{JsonArray, JsonObject}
import com.couchbase.client.scala.codec.JsonSerializer.PlayEncode
import com.couchbase.client.scala.codec.JsonSerializer.JsonObjectConvert
import Implicits._
import com.couchbase.client.scala.codec.JsonSerializer
import play.api.libs.json.Json

class JsCodecSpec extends GuiceSpecBase {
  val key = "test_key"

  def jsGet = cb.bk1.getJsT[Trophy](key) must beEqualTo(Trophy.t1).await
  def jsonGet = cb.bk1
    .getT[JsonObject](key)
    .map(json => json.toPlayJs.as[Trophy]) must beEqualTo(
    Trophy.t1
  ).await

  implicit val ser: JsonSerializer[Trophy] = t =>
    PlayEncode.serialize(Json.toJson(t))

  def jsSet =
    cb.bk1.upsert(key, Trophy.t1).map(_.cas) must be_>(0L).await

  def jsonSet = {
    import Trophy.t1
    val arr = JsonArray.create
    for (d <- t1.d) {
      val a2 = JsonArray.create
      for (i <- d) a2.add(i)
      arr.add(a2)
    }
    val js = JsonObject.create
      .put("n", t1.n)
      .put("d", arr)
    cb.bk1.upsert(key, js).map(_.cas) must be_>(0L).await
  }

  "JsCodec" should {
    "upsert using JsTranscoder then get using JsonTranscoder/ JsTranscoder" in {
      jsSet
      jsonGet
//      jsGet
    }
//    "upsert using JsonTranscoder then get using JsonTranscoder/ JsTranscoder" in {
//      jsonSet
//      jsonGet
//      jsGet
//    }
  }
}
