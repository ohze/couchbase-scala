package com.sandinh.couchbase

import com.couchbase.client.core.error.DecodingFailureException
import com.couchbase.client.scala.codec.JsonSerializer.StringConvert
import com.couchbase.client.scala.codec._
import com.couchbase.client.scala.kv.{GetOptions, UpsertOptions}
import com.typesafe.config.Config
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.Random

class CompatStringSpec extends GuiceSpecBase {
  @Inject private var config: Config = _
  private lazy val bk1Compat = {
    val cluster = new CBCluster(config)
    cluster.bucket("bk1")
  }

  val id = "test_CompatStringSpec"
  val s = "ab!?-'yf89da3\"\"2$^$\""

  def set(idSuffix: Long, t: Transcoder): Future[String] =
    cb.bk1.upsert(id + idSuffix, s, UpsertOptions().transcoder(t)).map(_ => s)

  // default use JsonTranscoder
  def get(idSuffix: Long, t: Transcoder): Future[String] =
    cb.bk1
      .get(id + idSuffix, GetOptions().transcoder(t))
      .map(_.contentAs[String].get)

  "String transcoders" should {
    "1. set using RawStringTranscoder, get using other Transcoders" in {
      val suffix = Random.nextLong()
      set(suffix, RawStringTranscoder.Instance) must beEqualTo(s).await
      get(suffix, RawStringTranscoder.Instance) must beEqualTo(s).await
      get(suffix, LegacyTranscoder.Instance) must beEqualTo(s).await
      get(suffix, RawJsonTranscoder.Instance) must beEqualTo(s).await
      get(suffix, JsonTranscoder.Instance) must throwA[
        DecodingFailureException
      ].await
    }

    "2. set using LegacyTranscoder, get using other Transcoders" in {
      val suffix = Random.nextLong()
      set(suffix, LegacyTranscoder.Instance) must beEqualTo(s).await
      get(suffix, RawStringTranscoder.Instance) must beEqualTo(s).await
      get(suffix, LegacyTranscoder.Instance) must beEqualTo(s).await
      get(suffix, RawJsonTranscoder.Instance) must beEqualTo(s).await
      get(suffix, JsonTranscoder.Instance) must throwA[
        DecodingFailureException
      ].await
    }

    "3. set using RawJsonTranscoder, get using other Transcoders" in {
      val suffix = Random.nextLong()
      set(suffix, RawJsonTranscoder.Instance) must beEqualTo(s).await
      get(suffix, RawStringTranscoder.Instance) must beEqualTo(s).await
      get(suffix, LegacyTranscoder.Instance) must beEqualTo(s).await
      get(suffix, RawJsonTranscoder.Instance) must beEqualTo(s).await
      get(suffix, JsonTranscoder.Instance) must throwA[
        DecodingFailureException
      ].await
    }

    "4. set using JsonTranscoder, get using other Transcoders" in {
      val suffix = Random.nextLong()
      val expected = Json.toJson(s).toString()
      set(suffix, JsonTranscoder.Instance) must beEqualTo(s).await
      get(suffix, RawStringTranscoder.Instance) must beEqualTo(expected).await
      get(suffix, LegacyTranscoder.Instance) must beEqualTo(expected).await
      get(suffix, RawJsonTranscoder.Instance) must beEqualTo(expected).await
      get(suffix, JsonTranscoder.Instance) must beEqualTo(s).await
    }
  }
}
