package com.sandinh.couchbase

import java.lang
import javax.inject._

import com.couchbase.client.java.CouchbaseAsyncCluster
import com.couchbase.client.java.auth.{CertAuthenticator, PasswordAuthenticator}
import com.couchbase.client.java.document.Document
import com.couchbase.client.java.env.CouchbaseEnvironment
import com.couchbase.client.java.transcoder.Transcoder
import com.sandinh.couchbase.transcoder._
import com.typesafe.config.Config

import scala.jdk.CollectionConverters._
import scala.concurrent.{Await, Future}
import com.sandinh.couchbase.JavaConverters._
import com.sandinh.rx.Implicits._

import scala.concurrent.duration._

/** @note ensure call #disconnect() at the end of application life */
@Singleton
class CBCluster @Inject() (config: Config) {
  val env: CouchbaseEnvironment = CbEnvBuilder(config)

  val asJava: CouchbaseAsyncCluster = {
    val cfg = config.getConfig("com.sandinh.couchbase")
    val cluster = CouchbaseAsyncCluster.fromConnectionString(
      env,
      cfg.getString("connectionString")
    )
    if (!cfg.hasPath("user")) cluster
    else
      cluster.authenticate(
        new PasswordAuthenticator(
          cfg.getString("user"),
          cfg.getString("password")
        )
      )
  }

  /** Open bucket with typesafe config load from key com.sandinh.couchbase.buckets.`bucket`
    * @param bucket use as a subkey of typesafe config for open bucket.
    * @param legacyEncodeString set = true to choose CompatStringTranscoderLegacy, false to choose CompatStringTranscoder
    * @param transcoders extra customize transcoders.
    *
    * @note JsTranscoder & CompatStringTranscoderLegacy | CompatStringTranscoder is auto passed to underlying `CouchbaseAsyncCluster.openBucket`,
    * so don't need to be passed into `transcoders` param.
    *
    * @note couchbase will cache Bucket by name.
    * So, if you need both legacyEncodeString & not-legacyEncodeString transcoder then you MUST create another cluster.
    * see example in com.sandinh.couchbase.CompatStringSpec.bk1Compat
    */
  def openBucket(
    bucket: String,
    legacyEncodeString: Boolean,
    transcoders: Transcoder[_ <: Document[_], _]*
  ): Future[ScalaBucket] = {
    val cfg = config.getConfig("com.sandinh.couchbase")
    val name = s"buckets.$bucket.name" match {
      case p if cfg.hasPath(p) => cfg.getString(p)
      case _                   => bucket
    }
    val stringTranscoder =
      if (legacyEncodeString) CompatStringTranscoderLegacy
      else CompatStringTranscoder
    val trans = transcoders :+ JsTranscoder :+ stringTranscoder
    val bucketObs = asJava.authenticator() match {
      case _: PasswordAuthenticator | _: CertAuthenticator =>
        asJava.openBucket(name, trans.asJava)
      case _ =>
        val pass = cfg.getString(s"buckets.$bucket.password")
        asJava.openBucket(name, pass, trans.asJava)
    }
    bucketObs.scMap(_.asScala).toFuture
  }

  /** @note You should never perform long-running blocking operations inside of an asynchronous stream (e.g. inside of maps or flatMaps)
    * @see https://issues.couchbase.com/browse/JVMCBC-79
    */
  def openBucketSync(
    bucket: String,
    legacyEncodeString: Boolean,
    transcoders: Transcoder[_ <: Document[_], _]*
  ): ScalaBucket =
    Await.result(
      openBucket(bucket, legacyEncodeString, transcoders: _*),
      env.connectTimeout.millis
    )

  /** openBucket(bucket, legacyEncodeString = true) */
  def openBucket(bucket: String): Future[ScalaBucket] =
    openBucket(bucket, legacyEncodeString = true)

  /** openBucketSync(bucket, legacyEncodeString = true)
    * @note You should never perform long-running blocking operations inside of an asynchronous stream (e.g. inside of maps or flatMaps)
    * @see https://issues.couchbase.com/browse/JVMCBC-79
    */
  def openBucketSync(bucket: String): ScalaBucket =
    openBucketSync(bucket, legacyEncodeString = true)

  def disconnect(): Future[lang.Boolean] = asJava.disconnect().toFuture

  def disconnectSync(): Boolean = Await
    .result(
      asJava.disconnect().toFuture,
      env.disconnectTimeout.millis
    )
    .booleanValue
}

private object CbEnvBuilder {
  import java.util.concurrent.TimeUnit.MILLISECONDS
  import com.couchbase.client.java.env.DefaultCouchbaseEnvironment
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
      .build()
  }
}
