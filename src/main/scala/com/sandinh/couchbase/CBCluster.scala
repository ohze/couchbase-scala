package com.sandinh.couchbase

import javax.inject._
import com.couchbase.client.java.CouchbaseAsyncCluster
import com.couchbase.client.java.document.Document
import com.couchbase.client.java.transcoder.Transcoder
import com.sandinh.couchbase.transcoder._
import com.typesafe.config.Config
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.util.Try
import com.sandinh.couchbase.JavaConverters._
import com.sandinh.rx.Implicits._
import scala.concurrent.duration._

/** @note ensure call #disconnect() at the end of application life */
@Singleton
class CBCluster @Inject() (config: Config) {
  private val env = CbEnv(config)

  private val cluster: CouchbaseAsyncCluster =
    CouchbaseAsyncCluster.fromConnectionString(env, config.getString("com.sandinh.couchbase.connectionString"))

  /** Open bucket with typesafe config load from key com.sandinh.couchbase.buckets.`bucket`
    * @param bucket use as a subkey of typesafe config for open bucket.
    * @param legacyEncodeString set = true to choose CompatStringTranscoderLegacy, false to choose CompatStringTranscoder
    * @param transcoders extra customize transcoders.
    * Note JsTranscoder & CompatStringTranscoderLegacy | CompatStringTranscoder is auto passed to underlying `CouchbaseAsyncCluster.openBucket`,
    * so don't need to include into `transcoders` param */
  def openBucket(bucket: String, legacyEncodeString: Boolean, transcoders: Transcoder[_ <: Document[_], _]*): ScalaBucket = {
    val cfg = config.getConfig(s"com.sandinh.couchbase.buckets.$bucket")
    val name = Try { cfg.getString("name") } getOrElse bucket
    val pass = cfg.getString("password")
    val stringTranscoder = if (legacyEncodeString) CompatStringTranscoderLegacy else CompatStringTranscoder
    val trans = transcoders :+ JsTranscoder :+ stringTranscoder
    Await.result(
      cluster.openBucket(name, pass, trans.asJava).toFuture,
      env.connectTimeout.millis
    ).asScala
  }

  /** openBucket(bucket, legacyEncodeString = true) */
  def openBucket(bucket: String): ScalaBucket = openBucket(bucket, legacyEncodeString = true)

  def disconnect(): Boolean = Await.result(
    cluster.disconnect().toFuture,
    env.disconnectTimeout.millis
  ).booleanValue
}

private object CbEnv {
  import java.util.concurrent.TimeUnit.MILLISECONDS
  import com.couchbase.client.java.env.{CouchbaseEnvironment, DefaultCouchbaseEnvironment}
  import DefaultCouchbaseEnvironment.Builder

  def apply(config: Config): CouchbaseEnvironment = {
    val b = DefaultCouchbaseEnvironment.builder()
    val c = config.getConfig("com.couchbase.timeout")

    def set(k: String, f: Long => Builder): Builder =
      if (c.hasPath(k)) f(c.getDuration(k, MILLISECONDS)) else b

    set("management", b.managementTimeout)
    set("query", b.queryTimeout)
    set("view", b.viewTimeout)
    set("kv", b.kvTimeout)
    set("connect", b.connectTimeout)
    set("disconnect", b.disconnectTimeout)
    b.build()
  }
}
