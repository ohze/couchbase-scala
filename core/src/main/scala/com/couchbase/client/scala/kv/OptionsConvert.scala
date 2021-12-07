package com.couchbase.client.scala.kv

object OptionsConvert {
  implicit class InsertOptionsOps(private val o: InsertOptions) extends AnyVal {
    def toReplaceOptions(
      cas: Long = 0,
      preserveExpiry: Boolean = false
    ): ReplaceOptions =
      ReplaceOptions(
        cas,
        o.durability,
        o.timeout,
        o.parentSpan,
        o.retryStrategy,
        o.transcoder,
        o.expiry,
        o.expiryTime,
        preserveExpiry
      )
  }
}
