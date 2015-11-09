package com.sandinh.couchbase.document

import com.couchbase.client.core.message.kv.MutationToken
import com.couchbase.client.java.document.AbstractDocument
import play.api.libs.json.{Reads, Json, Writes, JsValue}

class JsDocument(
    id:            String        = null,
    content:       JsValue       = null,
    expiry:        Int           = 0,
    cas:           Long          = 0,
    mutationToken: MutationToken = null
) extends AbstractDocument[JsValue](id, expiry, content, cas, mutationToken) {
  @inline final def as[T: Reads]: T = content.as[T]
}

object JsDocument {
  def apply[T: Writes](
    id:            String        = null,
    content:       T             = null,
    expiry:        Int           = 0,
    cas:           Long          = 0,
    mutationToken: MutationToken = null
  ) = new JsDocument(id, Json.toJson(content), expiry, cas, mutationToken)
}
