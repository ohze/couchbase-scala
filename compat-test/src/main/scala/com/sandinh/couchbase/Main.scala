package com.sandinh.couchbase
import com.couchbase.client.java.document.JsonLongDocument
import com.sandinh.couchbase.Implicits.DocNotExistFuture
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    val cluster = new CBCluster(ConfigFactory.load())
    val bucket = cluster.openBucketSync("fodi")

    val f = args match {
      case Array("set", "counter", key, value) =>
        bucket.counter(key, 0, value.toLong)
      case Array("get", "counter", key) =>
        bucket
          .get[JsonLongDocument](key)
          .map(_.content.longValue)
          .recoverNotExist(0L)
      case _ => ???
    }

    val v = Await.result(f, 5.seconds)
    print(v)
  }
}
