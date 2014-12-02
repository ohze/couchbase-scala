package com.sandinh.couchbase

import javax.inject._
import com.couchbase.client.core.logging.CouchbaseLoggerFactory
import com.couchbase.client.java.CouchbaseAsyncCluster
import com.couchbase.client.java.document.Document
import com.couchbase.client.java.transcoder.Transcoder
import com.typesafe.config.Config
import rx.lang.scala.Observable
import scala.collection.JavaConverters._
import scala.util.Try
import com.sandinh.couchbase.JavaConverters._
import com.sandinh.rx.Implicits._

/** @note ensure call #disconnect() at the end of application life */
@Singleton
class CBCluster @Inject() (config: Config) {
  private[this] val cluster: CouchbaseAsyncCluster = {
    CBCluster.config2SystemEnv(config, "com.couchbase")
    CouchbaseAsyncCluster.fromConnectionString(config.getString("com.sandinh.couchbase.connectionString"))
  }

  def openBucket(bucket: String, transcoder: Transcoder[_ <: Document[_], _]*): Observable[ScalaBucket] = {
    val cfg = config.getConfig(s"com.sandinh.couchbase.buckets.$bucket")
    val name = Try { cfg.getString("name") } getOrElse bucket
    val pass = cfg.getString("password")
    cluster.openBucket(name, pass, transcoder.asJava).asScala.map(_.asScala)
  }

  def disconnect(): Observable[Boolean] = cluster.disconnect().asScala.map(_.booleanValue)
}

object CBCluster {
  private lazy val logger = CouchbaseLoggerFactory.getInstance(classOf[CBCluster])

  private def config2SystemEnv(config: Config, path: String): Unit = {
    import com.typesafe.config.ConfigValueType._

    for (entry <- config.getConfig(path).entrySet().asScala) {
      val key = path + "." + entry.getKey
      entry.getValue.valueType match {
        case NUMBER | BOOLEAN | STRING => System.setProperty(key, entry.getValue.unwrapped.toString)
        case _                         => logger.warn("unsupported config {}", key)
      }
    }
  }
}
