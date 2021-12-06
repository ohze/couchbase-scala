package com.sandinh.couchbase

import com.couchbase.client.scala.env.TimeoutConfig
import com.couchbase.client.scala.ClusterOptions
import com.couchbase.client.scala.env.{
  ClusterEnvironment,
  PasswordAuthenticator
}
import com.couchbase.client.scala.AsyncCluster
import com.typesafe.config.Config

import javax.inject._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/** @note ensure call #disconnect() at the end of application life */
@Singleton
class CBCluster @Inject() (config: Config) {
  val env: ClusterEnvironment = CbEnvBuilder(config)
  private val conf = config.getConfig("com.sandinh.couchbase")

  @deprecated("Use underlying", "10.0.0")
  def asJava: AsyncCluster = underlying

  lazy val underlying: AsyncCluster =
    AsyncCluster
      .connect(
        conf.getString("connectionString"),
        ClusterOptions(
          PasswordAuthenticator(
            conf.getString("user"),
            conf.getString("password")
          ),
          Some(env)
        ),
      )
      .get

  def bucket(bucketName: String): CBBucket =
    new CBBucket(underlying.bucket(bucketName), underlying)

  @deprecated("Use bucket", "10.0.0")
  def openBucket(bucketName: String): CBBucket = bucket(bucketName)

  @deprecated("Use bucket", "10.0.0")
  def openBucketSync(bucketName: String): CBBucket = bucket(bucketName)

  def disconnect(): Future[Unit] = underlying.disconnect()

  def disconnectSync(): Unit = Await
    .result(
      disconnect(),
      env.core.timeoutConfig().disconnectTimeout.toNanos.nanos
    )
}

private object CbEnvBuilder {
  def apply(config: Config): ClusterEnvironment = {
    val conf = config.getConfig("com.couchbase.timeout")
    def c(k: String): Option[Duration] =
      if (conf.hasPath(k)) Some(conf.getDuration(k).toNanos.nanos)
      else None
    val timeoutConfig = TimeoutConfig(
      c("kv"),
      c("kvDurable"),
      c("management"),
      c("query"),
      c("view"),
      c("search"),
      c("analytics"),
      c("connect"),
      c("disconnect")
    )
    ClusterEnvironment
      .Builder(owned = true)
      .timeoutConfig(timeoutConfig)
      .build
      .get
  }
}
