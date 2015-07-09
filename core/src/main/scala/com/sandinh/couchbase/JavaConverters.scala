package com.sandinh.couchbase

import com.couchbase.client.java.AsyncBucket

object JavaConverters {
  implicit class BucketAsScala(val underlying: AsyncBucket) extends AnyVal {
    def asScala = new ScalaBucket(underlying)
  }
}
