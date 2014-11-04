package com.sandinh.couchbase

import javax.inject._
import com.couchbase.client.core.logging.CouchbaseLoggerFactory
import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.document.Document
import com.couchbase.client.java.transcoder.Transcoder
import com.typesafe.config.Config
import scala.collection.JavaConverters._
import scala.util.Try
import com.sandinh.couchbase.JavaConverters._

/** @note ensure call cluster.disconnect() at the end of application life */
@Singleton
class CBCluster @Inject() (config: Config) {
  //lazy?
  val cluster: CouchbaseCluster = {
    CBCluster.config2SystemEnv(config, "com.couchbase")
    CouchbaseCluster.fromConnectionString(config.getString("com.sandinh.couchbase.connectionString"))
  }

  def openBucket(bucket: String, transcoder: Transcoder[_ <: Document[_], _]*): ScalaBucket = {
    val cfg = config.getConfig(s"com.sandinh.couchbase.buckets.$bucket")
    val name = Try { cfg.getString("name") } getOrElse bucket
    val pass = cfg.getString("password")
    cluster.openBucket(name, pass, transcoder.asJava).async().asScala
  }
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
