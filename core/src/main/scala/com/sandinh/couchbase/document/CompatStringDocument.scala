package com.sandinh.couchbase.document

import com.couchbase.client.java.document.AbstractDocument

/** Stores a properly encoded JSON scalar quoted string as the toplevel type.
  *
  * This document:
  * + works exactly like JsonDocument, but it accepts a different toplevel type.
  * + is interoperable with other SDKs.
  * + not like JsonStringDocument: This document permit decoding a stored StringDocument */
class CompatStringDocument(
  id:      String = null,
  content: String = null,
  expiry:  Int    = 0,
  cas:     Long   = 0
) extends AbstractDocument[String](id, expiry, content, cas)
