/** @author giabao
  * created: 2013-10-22 10:23
  * (c) 2011-2013 sandinh.com
  */
package com.sandinh.couchbase

import com.couchbase.client.scala.codec.JsonSerializer.JsonObjectConvert
import com.couchbase.client.scala.json.{JsonArray, JsonObject}
import com.sandinh.couchbase.Implicits._
import org.specs2.matcher.MatchResult

import scala.concurrent.Future
import scala.util.Random

class JsCodecSpec extends GuiceSpecBase {
  val id = "test_key"

  def jsGet(idSuffix: Long): MatchResult[Future[Trophy]] =
    cb.bk1.getJsT[Trophy](id + idSuffix) must beEqualTo(Trophy.t1).await

  def jsonGet(idSuffix: Long): MatchResult[Future[Trophy]] =
    cb.bk1
      .getT[JsonObject](id + idSuffix)
      .map(json => json.toPlayJs.as[Trophy]) must beEqualTo(Trophy.t1).await

  def jsSet(idSuffix: Long): MatchResult[Future[Long]] =
    cb.bk1.upsert(id + idSuffix, Trophy.t1).map(_.cas) must be_>(0L).await

  def jsonSet(idSuffix: Long): MatchResult[Future[Long]] = {
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
    cb.bk1.upsert(id + idSuffix, js).map(_.cas) must be_>(0L).await
  }

  "JsCodec" should {
    "upsert using PlayEncode + play-json then get using play-json or JsonObject" in {
      val suffix = Random.nextLong()
      jsSet(suffix)
      jsGet(suffix)
      jsonGet(suffix)
    }

    "upsert using JsonObject then get using play-json or JsonObject" in {
      val suffix = Random.nextLong()
      jsonSet(suffix)
      jsGet(suffix)
      jsonGet(suffix)
    }
  }
}
