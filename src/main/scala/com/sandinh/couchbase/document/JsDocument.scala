package com.sandinh.couchbase.document

import com.couchbase.client.java.document.AbstractDocument
import play.api.libs.json.{Json, Writes, JsValue}

class JsDocument(
  id:      String  = null,
  content: JsValue = null,
  expiry:  Int     = 0,
  cas:     Long    = 0
) extends AbstractDocument[JsValue](id, expiry, content, cas)

object JsDocument {
  def apply[T: Writes](
    id:      String = null,
    content: T      = null,
    expiry:  Int    = 0,
    cas:     Long   = 0
  ) = new JsDocument(id, Json.toJson(content), expiry, cas)
}
